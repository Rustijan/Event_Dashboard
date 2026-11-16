package com.epam.eventhub.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public class SessionForm {
    @NotNull(message = "Start date and time are required")
    @Future(message = "Start date and time must be in the future")
    private LocalDateTime startsAt;

    @NotNull(message = "End date and time are required")
    private LocalDateTime endsAt;
    @Positive(message = "Capacity must be greater than zero")
    private int capacity;

    public SessionForm() {}
    public SessionForm(LocalDateTime startsAt, LocalDateTime endsAt, int capacity) {
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.capacity = capacity;

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

}
