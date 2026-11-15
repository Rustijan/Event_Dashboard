package com.epam.eventhub.autocode;

import com.epam.eventhub.exception.AccessDeniedException;
import com.epam.eventhub.exception.BusinessRuleException;
import com.epam.eventhub.model.Event;
import com.epam.eventhub.model.RegistrationStatus;
import com.epam.eventhub.model.Role;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegistrationWorkflowAutocodeTest extends EventHubIntegrationTestSupport {

    @Test
    void firstRegistrationCreatesOneActiveRowAndDuplicateIsRejected() {
        long participantId = participantId();
        long sessionId = publishedSessionId();
        long eventId = id("SELECT event_id FROM event_sessions WHERE id = ?", sessionId);

        assertThat(registrationService.register(sessionId, participantId)).isEqualTo(eventId);
        assertThatThrownBy(() -> registrationService.register(sessionId, participantId))
                .isInstanceOf(BusinessRuleException.class);
        assertThat(registrationCount(sessionId, participantId)).isEqualTo(1);
        assertThat(activeRegistrationCount(sessionId)).isEqualTo(1);
    }

    @Test
    void cancellationThenRegistrationReactivatesTheSameRow() {
        long participantId = participantId();
        long sessionId = publishedSessionId();
        registrationService.register(sessionId, participantId);
        long registrationId = id(
                "SELECT id FROM registrations WHERE session_id = ? AND participant_id = ?",
                sessionId, participantId);

        registrationService.cancel(registrationId, participantId);
        assertThat(status(registrationId)).isEqualTo("CANCELLED");

        registrationService.register(sessionId, participantId);
        assertThat(status(registrationId)).isEqualTo("ACTIVE");
        assertThat(registrationCount(sessionId, participantId)).isEqualTo(1);
    }

    @Test
    void capacityIsEnforced() {
        long sessionId = publishedSessionId();
        jdbc.update("UPDATE event_sessions SET capacity = 1 WHERE id = ?", sessionId);
        long first = participantId();
        long second = registerUser("Second Participant", "second.participant@example.com", Role.PARTICIPANT);

        registrationService.register(sessionId, first);
        assertThatThrownBy(() -> registrationService.register(sessionId, second))
                .isInstanceOf(BusinessRuleException.class);
        assertThat(activeRegistrationCount(sessionId)).isEqualTo(1);
    }

    @Test
    void registrationRequiresParticipantPublishedEventAndFutureSession() {
        long participantId = participantId();
        long organizerId = id("SELECT id FROM users WHERE email = ?", ORGANIZER_EMAIL);
        long draftSessionId = id("""
                SELECT s.id FROM event_sessions s
                JOIN events e ON e.id = s.event_id
                WHERE e.title = 'Spring MVC Practice'
                """);

        assertThatThrownBy(() -> registrationService.register(draftSessionId, participantId))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> registrationService.register(publishedSessionId(), organizerId))
                .isInstanceOf(AccessDeniedException.class);

        long publishedSessionId = publishedSessionId();
        jdbc.update("UPDATE event_sessions SET starts_at = ?, ends_at = ? WHERE id = ?",
                java.time.LocalDateTime.now().minusHours(2), java.time.LocalDateTime.now().minusHours(1),
                publishedSessionId);
        assertThatThrownBy(() -> registrationService.register(publishedSessionId, participantId))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void cancellationChecksOwnershipAndSessionTime() {
        long ownerId = participantId();
        long otherId = registerUser("Other Participant", "other.participant@example.com", Role.PARTICIPANT);
        long sessionId = publishedSessionId();
        registrationService.register(sessionId, ownerId);
        long registrationId = id("SELECT id FROM registrations WHERE session_id = ? AND participant_id = ?",
                sessionId, ownerId);

        assertThatThrownBy(() -> registrationService.cancel(registrationId, otherId))
                .isInstanceOf(AccessDeniedException.class);

        jdbc.update("UPDATE event_sessions SET starts_at = ?, ends_at = ? WHERE id = ?",
                java.time.LocalDateTime.now().minusHours(2), java.time.LocalDateTime.now().minusHours(1), sessionId);
        assertThatThrownBy(() -> registrationService.cancel(registrationId, ownerId))
                .isInstanceOf(BusinessRuleException.class);
        assertThat(status(registrationId)).isEqualTo("ACTIVE");
    }

    @Test
    void cancellingEventAlsoCancelsActiveRegistrations() {
        long organizerId = id("SELECT id FROM users WHERE email = ?", ORGANIZER_EMAIL);
        long participantId = participantId();
        Event event = createDraft(organizerId, "Cancellable Event");
        long sessionId = addFutureSession(event.getId(), organizerId, 10).getId();
        eventService.publish(event.getId(), organizerId);
        registrationService.register(sessionId, participantId);

        eventService.cancel(event.getId(), organizerId);

        assertThat(jdbc.queryForObject("SELECT status FROM events WHERE id = ?", String.class, event.getId()))
                .isEqualTo("CANCELLED");
        assertThat(jdbc.queryForObject("SELECT status FROM registrations WHERE session_id = ?", String.class, sessionId))
                .isEqualTo("CANCELLED");
        assertThat(registrationService.findActiveForSession(sessionId)).isEmpty();
        assertThat(registrationService.findForParticipant(participantId))
                .extracting(registration -> registration.getStatus())
                .containsExactly(RegistrationStatus.CANCELLED);
    }

    private long participantId() {
        return id("SELECT id FROM users WHERE email = ?", PARTICIPANT_EMAIL);
    }

    private long publishedSessionId() {
        return id("""
                SELECT MIN(s.id) FROM event_sessions s
                JOIN events e ON e.id = s.event_id
                WHERE e.title = 'Java Community Evening' AND e.status = 'PUBLISHED'
                """);
    }

    private int registrationCount(long sessionId, long participantId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM registrations WHERE session_id = ? AND participant_id = ?",
                Integer.class, sessionId, participantId);
        return count == null ? 0 : count;
    }

    private int activeRegistrationCount(long sessionId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM registrations WHERE session_id = ? AND status = 'ACTIVE'",
                Integer.class, sessionId);
        return count == null ? 0 : count;
    }

    private String status(long registrationId) {
        return jdbc.queryForObject("SELECT status FROM registrations WHERE id = ?", String.class, registrationId);
    }
}
