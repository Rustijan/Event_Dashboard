package com.epam.eventhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EventForm {
    @NotBlank(message ="Event title is required")
    private String title;
    @NotBlank(message = "Event description is required")
    private String description;
    @NotNull(message = "Category is required")
    private Long categoryId;
    @NotNull(message = "Location is required")
    private Long locationId;


    public EventForm(String title, String description, Long categoryId, Long locationId) {
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
        this.locationId = locationId;
    }
    public EventForm() {}

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
    public Long getCategoryId() {
        return categoryId;
    }
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    public Long getLocationId() {
        return locationId;
    }
    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }






}
