package com.epam.eventhub.service;

import com.epam.eventhub.dto.EventForm;
import com.epam.eventhub.dto.EventSearchCriteria;
import com.epam.eventhub.dto.SessionForm;
import com.epam.eventhub.model.Event;
import com.epam.eventhub.model.EventSession;

import java.util.List;

public interface EventService {
    List<Event> findPublished(EventSearchCriteria criteria);
    Event getById(long eventId);
    List<EventSession> getSessions(long eventId);
    EventSession requireSessionOwner(long sessionId, long organizerId);
    List<Event> findForOrganizer(long organizerId);
    Event create(EventForm form, long organizerId);
    void update(long eventId, EventForm form, long organizerId);
    EventSession addSession(long eventId, SessionForm form, long organizerId);
    void publish(long eventId, long organizerId);
    void cancel(long eventId, long organizerId);
    Event requireOwner(long eventId, long organizerId);
}
