package com.epam.eventhub.dto;

public class EventSearchCriteria {
    private String query;
    private Long categoryId;
    private String city;


    public EventSearchCriteria(){}
    public EventSearchCriteria(String query, Long categoryId, String city) {
        this.query = query;
        this.categoryId = categoryId;
        this.city = city;
    }

    public String getQuery() {
        return query;
    }
    public void setQuery(String query) {
        this.query = query;
    }
    public Long getCategoryId() {
        return categoryId;
    }
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }

}
