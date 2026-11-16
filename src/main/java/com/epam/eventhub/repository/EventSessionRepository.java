package com.epam.eventhub.repository;

import com.epam.eventhub.dto.SessionForm;
import com.epam.eventhub.model.EventSession;

import java.util.List;
import java.util.Optional;

public interface EventSessionRepository {
    Optional<EventSession> findById(long id);
    List<EventSession> findByEventId(long eventId);
    EventSession save(long eventId, SessionForm form);
}
