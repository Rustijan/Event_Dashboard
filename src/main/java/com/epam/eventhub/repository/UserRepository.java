package com.epam.eventhub.repository;

import com.epam.eventhub.model.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(long id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    User save(User user);
}
