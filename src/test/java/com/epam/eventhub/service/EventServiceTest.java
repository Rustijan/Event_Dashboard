package com.epam.eventhub.service;

import com.epam.eventhub.dto.SessionForm;
import com.epam.eventhub.exception.BusinessRuleException;
import com.epam.eventhub.model.Category;
import com.epam.eventhub.model.Event;
import com.epam.eventhub.model.EventStatus;
import com.epam.eventhub.model.Location;
import com.epam.eventhub.repository.EventRepository;
import com.epam.eventhub.repository.EventSessionRepository;
import com.epam.eventhub.repository.RegistrationRepository;
import com.epam.eventhub.service.impl.EventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventServiceTest {
    private EventRepository eventRepository;
    private EventSessionRepository sessionRepository;
    private RegistrationRepository registrationRepository;
    private EventService service;

    @BeforeEach
    void setUp() {
        eventRepository = mock(EventRepository.class);
        sessionRepository = mock(EventSessionRepository.class);
        registrationRepository = mock(RegistrationRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2030-01-10T10:00:00Z"), ZoneOffset.UTC);
        service = new EventServiceImpl(eventRepository, sessionRepository, registrationRepository, clock);
    }

    @Test
    void publishRejectsDraftWithoutSessions() {
        Event event = event(EventStatus.DRAFT, 10, 50);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(sessionRepository.findByEventId(1L)).thenReturn(List.of());

        assertThrows(BusinessRuleException.class, () -> service.publish(1L, 10L));

        verify(eventRepository, never()).updateStatus(1L, EventStatus.PUBLISHED);
    }

    @Test
    void addingSessionRejectsCapacityAboveLocationLimit() {
        Event event = event(EventStatus.DRAFT, 10, 30);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        SessionForm form = new SessionForm();
        form.setStartsAt(LocalDateTime.of(2030, 1, 12, 10, 0));
        form.setEndsAt(LocalDateTime.of(2030, 1, 12, 12, 0));
        form.setCapacity(31);

        assertThrows(BusinessRuleException.class, () -> service.addSession(1L, form, 10L));

        verify(sessionRepository, never()).save(1L, form);
    }

    @Test
    void cancellingEventAlsoCancelsItsRegistrations() {
        Event event = event(EventStatus.PUBLISHED, 10, 50);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        service.cancel(1L, 10L);

        verify(eventRepository).updateStatus(1L, EventStatus.CANCELLED);
        verify(registrationRepository).cancelActiveByEventId(1L);
    }

    private Event event(EventStatus status, long organizerId, int locationCapacity) {
        return new Event(1L, "Java evening", "A sufficiently long event description",
                new Category(1L, "Technology"),
                new Location(1L, "Hall", "Minsk", "Main Street", locationCapacity),
                organizerId, "Organizer", status);
    }
}
