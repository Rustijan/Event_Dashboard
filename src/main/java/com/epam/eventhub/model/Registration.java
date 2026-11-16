
package com.epam.eventhub.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Registration {
    private Long id;
    private RegistrationStatus status;
    private LocalDateTime registeredAt;
    private Long sessionId;
    private Long participantId;
    private String participantName;
    private String participantEmail;
    private String eventTitle;
    private LocalDateTime sessionStartTime;

    public Registration() {}

    public Registration(Long id, RegistrationStatus status,
                        LocalDateTime registeredAt, Long sessionId,
                        Long participantId, String participantName,
                        String participantEmail, String eventTitle,
                        LocalDateTime sessionStartTime) {
        this.id = id;
        this.status = status;
        this.registeredAt = registeredAt;
        this.sessionId = sessionId;
        this.participantId = participantId;
        this.participantName = participantName;
        this.participantEmail = participantEmail;
        this.eventTitle = eventTitle;
        this.sessionStartTime = sessionStartTime;
    }
    public Registration(
            Long id,
            Long sessionId,
            Long participantId,
            String participantName,
            String participantEmail,
            String eventTitle,
            LocalDateTime sessionStartTime,
            RegistrationStatus status,
            LocalDateTime registeredAt) {

        this(id, status, registeredAt, sessionId, participantId,
                participantName, participantEmail, eventTitle,
                sessionStartTime);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getParticipantId() {
        return participantId;
    }

    public void setParticipantId(Long participantId) {
        this.participantId = participantId;
    }

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public String getParticipantEmail() {
        return participantEmail;
    }

    public void setParticipantEmail(String participantEmail) {
        this.participantEmail = participantEmail;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public LocalDateTime getSessionStartTime() {
        return sessionStartTime;
    }

    public void setSessionStartTime(LocalDateTime sessionStartTime) {
        this.sessionStartTime = sessionStartTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, status, registeredAt, sessionId,
                participantId, participantName, participantEmail,
                eventTitle, sessionStartTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Registration other = (Registration) o;

        return Objects.equals(id, other.id)
                && Objects.equals(status, other.status)
                && Objects.equals(registeredAt, other.registeredAt)
                && Objects.equals(sessionId, other.sessionId)
                && Objects.equals(participantId, other.participantId)
                && Objects.equals(participantName, other.participantName)
                && Objects.equals(participantEmail, other.participantEmail)
                && Objects.equals(eventTitle, other.eventTitle)
                && Objects.equals(sessionStartTime, other.sessionStartTime);
    }

    @Override
    public String toString() {
        return "Registration [id=" + id
                + ", status=" + status
                + ", registeredAt=" + registeredAt
                + ", sessionId=" + sessionId
                + ", participantId=" + participantId
                + ", participantName=" + participantName
                + ", participantEmail=" + participantEmail
                + ", eventTitle=" + eventTitle
                + ", sessionStartTime=" + sessionStartTime + "]";
    }
}
