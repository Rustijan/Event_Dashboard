package com.epam.eventhub.service;

import com.epam.eventhub.model.Registration;

import java.util.List;

public interface RegistrationService {
    long register(long sessionId, long participantId); // returns the event ID
    void cancel(long registrationId, long participantId);
    List<Registration> findForParticipant(long participantId);
    List<Registration> findActiveForSession(long sessionId);
}
