package com.epam.eventhub.service;

import com.epam.eventhub.exception.BusinessRuleException;
import com.epam.eventhub.model.Category;
import com.epam.eventhub.model.Event;
import com.epam.eventhub.model.EventSession;
import com.epam.eventhub.model.EventStatus;
import com.epam.eventhub.model.Location;
import com.epam.eventhub.model.Registration;
import com.epam.eventhub.model.RegistrationStatus;
import com.epam.eventhub.model.Role;
import com.epam.eventhub.model.User;
import com.epam.eventhub.repository.EventRepository;
import com.epam.eventhub.repository.EventSessionRepository;
import com.epam.eventhub.repository.RegistrationRepository;
import com.epam.eventhub.repository.UserRepository;
import com.epam.eventhub.service.impl.RegistrationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegistrationServiceTest {
    private RegistrationRepository registrationRepository;
    private EventSessionRepository sessionRepository;
    private EventRepository eventRepository;
    private UserRepository userRepository;
    private RegistrationService service;

    private final Clock clock = Clock.fixed(Instant.parse("2026-08-31T10:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        registrationRepository = mock(RegistrationRepository.class);
        sessionRepository = mock(EventSessionRepository.class);
        eventRepository = mock(EventRepository.class);
        userRepository = mock(UserRepository.class);
        service = new RegistrationServiceImpl(registrationRepository, sessionRepository,
                eventRepository, userRepository, clock);
    }

    @Test
    void participantCanRegisterForAvailablePublishedSession() {
        prepareAvailableSession();
        when(registrationRepository.findBySessionAndParticipant(5L, 20L)).thenReturn(Optional.empty());
        when(registrationRepository.countActiveBySessionId(5L)).thenReturn(7);

        long eventId = service.register(5L, 20L);

        assertEquals(1L, eventId);
        verify(registrationRepository).create(5L, 20L);
    }

    @Test
    void duplicateActiveRegistrationIsRejected() {
        prepareAvailableSession();
        Registration existing = new Registration(9L, 5L, 20L, "Participant", "p@test.local",
                "Java Evening", LocalDateTime.of(2026, 9, 2, 18, 0),
                RegistrationStatus.ACTIVE, LocalDateTime.of(2026, 8, 30, 10, 0));
        when(registrationRepository.findBySessionAndParticipant(5L, 20L)).thenReturn(Optional.of(existing));

        assertThrows(BusinessRuleException.class, () -> service.register(5L, 20L));

        verify(registrationRepository, never()).create(5L, 20L);
    }

    @Test
    void registrationIsRejectedWhenSessionIsFull() {
        prepareAvailableSession();
        when(registrationRepository.findBySessionAndParticipant(5L, 20L)).thenReturn(Optional.empty());
        when(registrationRepository.countActiveBySessionId(5L)).thenReturn(10);

        assertThrows(BusinessRuleException.class, () -> service.register(5L, 20L));

        verify(registrationRepository, never()).create(5L, 20L);
    }

    private void prepareAvailableSession() {
        User participant = new User(20L, "Participant", "p@test.local", "hash", Role.PARTICIPANT);
        EventSession session = new EventSession(5L, 1L,
                LocalDateTime.of(2026, 9, 2, 18, 0),
                LocalDateTime.of(2026, 9, 2, 20, 0), 10, 0);
        Event event = new Event(1L, "Java Evening", "A sufficiently long event description",
                new Category(1L, "Technology"), new Location(1L, "Hall", "Minsk", "Main", 50),
                10L, "Organizer", EventStatus.PUBLISHED);
        when(userRepository.findById(20L)).thenReturn(Optional.of(participant));
        when(sessionRepository.findById(5L)).thenReturn(Optional.of(session));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
    }
}
