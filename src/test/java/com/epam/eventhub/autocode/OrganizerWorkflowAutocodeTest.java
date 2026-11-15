package com.epam.eventhub.autocode;

import com.epam.eventhub.dto.EventForm;
import com.epam.eventhub.dto.SessionForm;
import com.epam.eventhub.exception.AccessDeniedException;
import com.epam.eventhub.exception.BusinessRuleException;
import com.epam.eventhub.model.Event;
import com.epam.eventhub.model.EventStatus;
import com.epam.eventhub.model.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrganizerWorkflowAutocodeTest extends EventHubIntegrationTestSupport {

    @Test
    void ownerCanCreateAddSessionAndPublishDraft() {
        long organizerId = id("SELECT id FROM users WHERE email = ?", ORGANIZER_EMAIL);
        Event event = createDraft(organizerId, "AutoCode Event");
        assertThat(event.getStatus()).isEqualTo(EventStatus.DRAFT);

        addFutureSession(event.getId(), organizerId, 20);
        eventService.publish(event.getId(), organizerId);

        assertThat(eventService.getById(event.getId()).getStatus()).isEqualTo(EventStatus.PUBLISHED);
        assertThat(eventService.getSessions(event.getId())).hasSize(1);
    }

    @Test
    void publicationNeedsASessionAndManagementNeedsTheOwner() {
        long ownerId = id("SELECT id FROM users WHERE email = ?", ORGANIZER_EMAIL);
        long otherOrganizerId = registerUser("Other Organizer", "other.organizer@example.com", Role.ORGANIZER);
        Event event = createDraft(ownerId, "Owned Draft");

        assertThatThrownBy(() -> eventService.publish(event.getId(), ownerId))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> eventService.requireOwner(event.getId(), otherOrganizerId))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> eventService.update(event.getId(), validEventForm(), otherOrganizerId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void sessionDatesAndCapacityAreValidatedByTheService() {
        long organizerId = id("SELECT id FROM users WHERE email = ?", ORGANIZER_EMAIL);
        Event event = createDraft(organizerId, "Validated Sessions");

        SessionForm past = session(LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1), 10);
        SessionForm reversed = session(LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(4), 10);
        SessionForm zero = session(LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(5).plusHours(1), 0);
        SessionForm tooLarge = session(LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(5).plusHours(1), 41);

        assertThatThrownBy(() -> eventService.addSession(event.getId(), past, organizerId))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> eventService.addSession(event.getId(), reversed, organizerId))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> eventService.addSession(event.getId(), zero, organizerId))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> eventService.addSession(event.getId(), tooLarge, organizerId))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void publishedEventCannotBeEditedOrGivenAnotherSession() {
        long organizerId = id("SELECT id FROM users WHERE email = ?", ORGANIZER_EMAIL);
        Event event = createDraft(organizerId, "Immutable Published Event");
        addFutureSession(event.getId(), organizerId, 10);
        eventService.publish(event.getId(), organizerId);

        assertThatThrownBy(() -> eventService.update(event.getId(), validEventForm(), organizerId))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> addFutureSession(event.getId(), organizerId, 10))
                .isInstanceOf(BusinessRuleException.class);
    }

    private EventForm validEventForm() {
        EventForm form = new EventForm();
        form.setTitle("Updated Event");
        form.setDescription("A valid updated description containing more than twenty characters.");
        form.setCategoryId(id("SELECT id FROM categories WHERE name = 'Education'"));
        form.setLocationId(id("SELECT id FROM locations WHERE name = 'Community Room'"));
        return form;
    }

    private SessionForm session(LocalDateTime startsAt, LocalDateTime endsAt, int capacity) {
        SessionForm form = new SessionForm();
        form.setStartsAt(startsAt);
        form.setEndsAt(endsAt);
        form.setCapacity(capacity);
        return form;
    }
}
