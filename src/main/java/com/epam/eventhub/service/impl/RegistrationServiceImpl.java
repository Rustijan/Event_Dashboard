package com.epam.eventhub.service.impl;

import com.epam.eventhub.exception.AccessDeniedException;
import com.epam.eventhub.exception.BusinessRuleException;
import com.epam.eventhub.model.*;
import com.epam.eventhub.repository.EventRepository;
import com.epam.eventhub.repository.EventSessionRepository;
import com.epam.eventhub.repository.RegistrationRepository;
import com.epam.eventhub.repository.UserRepository;
import com.epam.eventhub.service.RegistrationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RegistrationServiceImpl implements RegistrationService {
    private final RegistrationRepository registrationRepository;
    private final EventSessionRepository eventSessionRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public RegistrationServiceImpl(RegistrationRepository registrationRepository,
                                   EventSessionRepository eventSessionRepository,
                                   EventRepository eventRepository,
                                   UserRepository userRepository,
                                   Clock clock) {
        this.registrationRepository = registrationRepository;
        this.eventSessionRepository = eventSessionRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.clock = clock;

    }


    @Override
    @Transactional
    public long register(long sessionId, long participantId){
        EventSession session = eventSessionRepository.findById(sessionId)
                .orElseThrow(
                        ()->new IllegalArgumentException("Session not found")
                );

        Event event = eventRepository.findById(session.getEventId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Event not found")
                );
        var participant = userRepository.findById(participantId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Participant not found")
                );
        if(participant.getRole() != Role.PARTICIPANT){
            throw new BusinessRuleException("Only participants allowed to register");
        }
        if(event.getStatus() != EventStatus.PUBLISHED){
            throw new BusinessRuleException("Only published events accept registrations");
        }
        if(!session.getStartsAt().isAfter(LocalDateTime.now(clock))){
            throw new BusinessRuleException("Session has already been started");
        }

        var registration = registrationRepository.findBySessionAndParticipant(sessionId, participantId);

        if(registration.isPresent()){
            Registration real =  registration.get();
            if(real.getStatus() == RegistrationStatus.ACTIVE){
                throw new BusinessRuleException("Participant already registered");
            }
            if(registrationRepository.countActiveBySessionId(sessionId)>=session.getCapacity()){
                throw new BusinessRuleException("Capacity of session is full");
            }
            registrationRepository.reactivate(real.getId());
            return session.getEventId();
        }
        if(registrationRepository.countActiveBySessionId(sessionId)>= session.getCapacity()){
            throw new BusinessRuleException("Capacity of session is full");
        }
        registrationRepository.create(sessionId, participantId);
        return session.getEventId();
    }

    @Override
    @Transactional
    public void cancel(long registrationId, long participantId){
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Registration not found")
                );
        if(registration.getParticipantId() != participantId){
            throw new AccessDeniedException("You can't cancel someone else registration");
        }
        if(registration.getStatus() != RegistrationStatus.ACTIVE){
            throw new IllegalArgumentException("Registration has already been cancelled");
        }
        EventSession session = eventSessionRepository
                .findById(registration.getSessionId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Session not found")
                );
        if(!session.getStartsAt().isAfter(LocalDateTime.now(clock))){
            throw new BusinessRuleException("Can't cancel the session that has already been started");
        }
        registrationRepository.cancel(registrationId);
    }
    @Override
    public List<Registration> findForParticipant(long participantId){
        return List.copyOf(registrationRepository.findByParticipantId(participantId));
    }
    @Override
    public List<Registration> findActiveForSession(long sessionId){
        return List.copyOf(registrationRepository.findActiveBySessionId(sessionId));
    }
}
