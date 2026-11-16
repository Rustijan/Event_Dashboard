package com.epam.eventhub.repository;

import com.epam.eventhub.model.Registration;

import java.util.List;
import java.util.Optional;

public interface RegistrationRepository {
    Optional<Registration> findById(long id);
    Optional<Registration> findBySessionAndParticipant(long sessionId, long participantId);
    int countActiveBySessionId(long sessionId);
    Registration create(long sessionId, long participantId);
    void reactivate(long registrationId);
    void cancel(long registrationId);
    void cancelActiveByEventId(long eventId);
    List<Registration> findByParticipantId(long participantId);
    List<Registration> findActiveBySessionId(long sessionId);
}