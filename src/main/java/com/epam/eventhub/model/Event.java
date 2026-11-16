
package com.epam.eventhub.model;

import java.util.Objects;

public class Event {
    private Long id;
    private String title;
    private String description;
    private EventStatus status;
    private Category category;
    private Location location;
    private Long organizerId;
    private String organizerName;

    public Event() {}

    public Event(Long id, String title, String description, EventStatus status,
                 Category category, Location location,
                 Long organizerId, String organizerName) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.category = category;
        this.location = location;
        this.organizerId = organizerId;
        this.organizerName = organizerName;
    }
    public Event(
            Long id,
            String title,
            String description,
            Category category,
            Location location,
            Long organizerId,
            String organizerName,
            EventStatus status) {

        this(id, title, description, status, category, location,
                organizerId, organizerName);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Long getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(Long organizerId) {
        this.organizerId = organizerId;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, status,
                category, location, organizerId, organizerName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Event other = (Event) obj;

        return Objects.equals(id, other.id)
                && Objects.equals(title, other.title)
                && Objects.equals(description, other.description)
                && Objects.equals(status, other.status)
                && Objects.equals(category, other.category)
                && Objects.equals(location, other.location)
                && Objects.equals(organizerId, other.organizerId)
                && Objects.equals(organizerName, other.organizerName);
    }

    @Override
    public String toString() {
        return "Event [id=" + id
                + ", title=" + title
                + ", description=" + description
                + ", status=" + status
                + ", category=" + category
                + ", location=" + location
                + ", organizerId=" + organizerId
                + ", organizerName=" + organizerName + "]";
    }
}
