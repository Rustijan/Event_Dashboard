package com.epam.eventhub.repository;

import com.epam.eventhub.model.Category;

import java.util.List;

public interface CategoryRepository {
    List<Category> findAll();

}
