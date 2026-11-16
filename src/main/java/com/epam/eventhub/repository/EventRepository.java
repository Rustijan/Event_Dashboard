package com.epam.eventhub.repository;

import com.epam.eventhub.dto.EventForm;
import com.epam.eventhub.dto.EventSearchCriteria;
import com.epam.eventhub.model.Event;
import com.epam.eventhub.model.EventStatus;

import java.util.List;
import java.util.Optional;

public interface EventRepository {
    Optional<Event> findById(long id);
    List<Event> findPublished(EventSearchCriteria criteria);
    List<Event> findByOrganizerId(long organizerId);
    Event save(EventForm form, long organizerId);
    void update(long eventId, EventForm form);
    void updateStatus(long eventId, EventStatus status);
}
