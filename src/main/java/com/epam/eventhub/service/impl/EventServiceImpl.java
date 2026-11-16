
package com.epam.eventhub.service.impl;

import com.epam.eventhub.dto.EventForm;
import com.epam.eventhub.dto.EventSearchCriteria;
import com.epam.eventhub.dto.SessionForm;
import com.epam.eventhub.exception.AccessDeniedException;
import com.epam.eventhub.exception.BusinessRuleException;
import com.epam.eventhub.exception.NotFoundException;
import com.epam.eventhub.model.Event;
import com.epam.eventhub.model.EventSession;
import com.epam.eventhub.model.EventStatus;
import com.epam.eventhub.repository.EventRepository;
import com.epam.eventhub.repository.EventSessionRepository;
import com.epam.eventhub.repository.RegistrationRepository;
import com.epam.eventhub.service.EventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
@Service
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final EventSessionRepository eventSessionRepository;
    private final RegistrationRepository registrationRepository;
    private final Clock clock;



    public EventServiceImpl(EventRepository eventRepository,
                            EventSessionRepository eventSessionRepository,
                            RegistrationRepository registrationRepository,
                            Clock clock) {
        this.eventRepository = eventRepository;
        this.eventSessionRepository = eventSessionRepository;
        this.registrationRepository = registrationRepository;
        this.clock = clock;
    }
    @Override
    public List<Event> findPublished(EventSearchCriteria criteria){
        return List.copyOf(eventRepository.findPublished(criteria));
    }
    @Override
    public Event getById(long eventId){
        return eventRepository.findById(eventId)
                .orElseThrow(
                        () -> new NotFoundException("Event not found")
                );
    }
    @Override
    public List<EventSession> getSessions(long eventId){
        getById(eventId);
        return List.copyOf(eventSessionRepository.findByEventId(eventId));
    }
    @Override
    public Event requireOwner(long eventId, long organizerId){
        Event event = getById(eventId);
        if(!event.getOrganizerId().equals(organizerId)){
            throw new AccessDeniedException("Access denied");
        }
        return event;
    }

    @Override
    public EventSession requireSessionOwner(long sessionId, long organizerId){
        EventSession eventSession = eventSessionRepository.findById(sessionId)
                .orElseThrow(
                        () -> new NotFoundException("Session not found")
                );
        Event event = requireOwner(eventSession.getEventId(), organizerId);
        return eventSession;

    }
    @Override
    public List<Event> findForOrganizer(long organizerId){
        return List.copyOf(eventRepository.findByOrganizerId(organizerId));
    }
    @Override
    public Event create(EventForm form, long organizerId){
        if (form == null) {
            throw new IllegalArgumentException("Event form is required");
        }
        return eventRepository.save(form, organizerId);
    }
    @Override
    public void update(long eventId, EventForm form, long organizerId){
        if (form == null) {
            throw new IllegalArgumentException("Event form is required");
        }
        Event event = requireOwner(eventId, organizerId);
        eventRepository.update(eventId, form);
    }
    @Override
    public EventSession addSession(
            long eventId, SessionForm form, long organizerId) {

        Event event = requireOwner(eventId, organizerId);

        if (form == null) {
            throw new IllegalArgumentException("Session form is required");
        }

        if (form.getStartsAt() == null || form.getEndsAt() == null) {
            throw new IllegalArgumentException(
                    "Session start and end times are required");
        }

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new BusinessRuleException(
                    "Sessions can only be added to draft events");
        }

        if (!form.getStartsAt().isAfter(LocalDateTime.now(clock))) {
            throw new BusinessRuleException(
                    "Session must start in the future");
        }

        if (!form.getEndsAt().isAfter(form.getStartsAt())) {
            throw new BusinessRuleException(
                    "Session end must be after its start");
        }

        if (form.getCapacity() <= 0) {
            throw new BusinessRuleException(
                    "Session capacity must be positive");
        }

        if (form.getCapacity() > event.getLocation().getCapacity()) {
            throw new BusinessRuleException(
                    "Session capacity cannot exceed the location capacity");
        }

        return eventSessionRepository.save(eventId, form);
    }
    @Override
    public void publish(long eventId, long organizerId){
        Event event = requireOwner(eventId, organizerId);
        if(!event.getStatus().equals(EventStatus.DRAFT)){
            throw new BusinessRuleException("Only draft events can be published");
        }
        List<EventSession> session = eventSessionRepository.findByEventId(eventId);
        if(session.isEmpty()){
            throw new BusinessRuleException("Event must have at least one session");
        }
        eventRepository.updateStatus(eventId, EventStatus.PUBLISHED);

    }
    @Override
    @Transactional
    public void cancel(long eventId, long organizerId){
        Event event = requireOwner(eventId, organizerId);
        if(event.getStatus().equals(EventStatus.CANCELLED)){
            throw new BusinessRuleException("Event is already cancelled");
        }
        if(event.getStatus().equals(EventStatus.COMPLETED)){
            throw new BusinessRuleException("Completed events cannot be cancelled");
        }
        registrationRepository.cancelActiveByEventId(eventId);

        eventRepository.updateStatus(eventId, EventStatus.CANCELLED);

    }
}
