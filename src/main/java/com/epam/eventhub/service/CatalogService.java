package com.epam.eventhub.service;

import com.epam.eventhub.model.Category;
import com.epam.eventhub.model.Location;

import java.util.List;

public interface CatalogService {
    List<Category> getCategories();
    List<Location> getLocations();
    List<String> getCities();
}
