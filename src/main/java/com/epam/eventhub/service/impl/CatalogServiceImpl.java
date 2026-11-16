package com.epam.eventhub.service.impl;

import com.epam.eventhub.model.Category;
import com.epam.eventhub.model.Location;
import com.epam.eventhub.repository.CategoryRepository;
import com.epam.eventhub.repository.LocationRepository;
import com.epam.eventhub.service.CatalogService;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class CatalogServiceImpl implements CatalogService {

    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    public CatalogServiceImpl(CategoryRepository categoryRepository,
                              LocationRepository locationRepository) {
        this.categoryRepository = categoryRepository;
        this.locationRepository = locationRepository;
    }

    @Override
    public List<Category> getCategories(){
        return List.copyOf(categoryRepository.findAll());
    }

    @Override
    public List<Location> getLocations(){
        return List.copyOf(locationRepository.findAll());
    }
    @Override
    public List<String> getCities(){
        return List.copyOf(locationRepository.findCities());
    }
}
