package com.epam.eventhub.repository;

import com.epam.eventhub.model.Location;

import java.util.List;

public interface LocationRepository {
    List<Location> findAll();
    List<String> findCities();
}
