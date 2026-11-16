package com.epam.eventhub.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class EventSession {
    private Long id;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private int capacity;
    private int activeRegistrations;
    private Long eventId;


    public EventSession() {}
    public EventSession(Long id, Long eventId,
                        LocalDateTime startsAt, LocalDateTime endsAt,
                        int capacity, int activeRegistrations) {
        this.id = id;
        this.eventId = eventId;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.capacity = capacity;
        this.activeRegistrations = activeRegistrations;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public LocalDateTime getStartsAt() {
        return startsAt;
    }
    public void setStartsAt(LocalDateTime startsAt) {
        this.startsAt = startsAt;
    }
    public LocalDateTime getEndsAt() {
        return endsAt;
    }
    public void setEndsAt(LocalDateTime endsAt) {
        this.endsAt = endsAt;
    }
    public int getCapacity() {
        return capacity;
    }
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
    public int getActiveRegistrations() {
        return activeRegistrations;
    }
    public void setActiveRegistrations(int activeRegistrations) {
        this.activeRegistrations = activeRegistrations;
    }
    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }


    public int getAvailablePlaces() {
        return Math.max(0, capacity - activeRegistrations);
    }
    public boolean isFull(){
        return  activeRegistrations >= capacity;
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, eventId, startsAt, endsAt,
                capacity, activeRegistrations);
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventSession other = (EventSession) o;
        return Objects.equals(this.id, other.id)
                && Objects.equals(this.eventId, other.eventId)
                && Objects.equals(this.startsAt, other.startsAt)
                && Objects.equals(this.endsAt, other.endsAt)
                && this.capacity == other.capacity
                && this.activeRegistrations == other.activeRegistrations;
    }

    @Override
    public String toString(){
        return "EventSession [id=" + id + ", startsAt=" + startsAt
                + ", endsAt=" + endsAt + ", capacity=" + capacity
                + ", activeRegistrations=" + activeRegistrations + "]";
    }
}
