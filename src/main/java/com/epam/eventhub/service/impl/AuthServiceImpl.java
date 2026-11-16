package com.epam.eventhub.service.impl;

import com.epam.eventhub.dto.RegistrationForm;
import com.epam.eventhub.exception.AuthenticationException;
import com.epam.eventhub.exception.BusinessRuleException;
import com.epam.eventhub.exception.DataAccessException;
import com.epam.eventhub.model.User;
import com.epam.eventhub.repository.UserRepository;
import com.epam.eventhub.service.AuthService;
import com.epam.eventhub.session.AuthenticatedUser;
import com.epam.eventhub.util.PasswordEncoder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Locale;
@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

    }

    @Override
    public AuthenticatedUser authenticate(String email, String password){
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(
                        () -> new AuthenticationException("Invalid email or password")
                );

        if(!passwordEncoder.matches(password, user.getPasswordHash())){
            throw new AuthenticationException("Invalid email or password");
        }
        return new AuthenticatedUser(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole()
        );
    }

    @Override
    public AuthenticatedUser register(RegistrationForm registrationForm){
        if(registrationForm == null){
            throw new IllegalArgumentException("RegistrationForm cannot be null");
        }
        String normalizedEmail = registrationForm.getEmail().trim().toLowerCase(Locale.ROOT);
        if(userRepository.existsByEmail(normalizedEmail)){
            throw new AuthenticationException("Email already exists");
        }

        String passwordHash = passwordEncoder.encode(registrationForm.getPassword());
        User user = new User(
                null,
                registrationForm.getFullName(),
                normalizedEmail,
                passwordHash,
                registrationForm.getRole()
        );

        try{
            User savedUser = userRepository.save(user);
            if(savedUser == null || savedUser.getId() ==null){
                throw new AuthenticationException("User repository did not return a saved user with an ID");
            }
            return new AuthenticatedUser(
                    savedUser.getId(),
                    savedUser.getFullName(),
                    savedUser.getEmail(),
                    savedUser.getRole()
            );
        }catch(DataAccessException exception){
            if(isDuplicateEmail(exception)){
                throw new BusinessRuleException("Email is already registered");
            }
            throw exception;
        }catch(DataIntegrityViolationException exception){
            throw new BusinessRuleException("Email is already registered");
        }
    }
    private boolean isDuplicateEmail(DataAccessException exception){
        Throwable cause = exception;
        while(cause != null){
            if(cause instanceof java.sql.SQLException sqlException
                        && "23505".equals(sqlException.getSQLState())){
                return true;
            }
            String message = cause.getMessage();
            if(message != null){
                String lowerMessage = message.toLowerCase(Locale.ROOT);
                if(lowerMessage.contains("duplicate")
                        || lowerMessage.contains("unique")
                        || lowerMessage.contains("already exists") ){
                    return true;
                }
            }
            cause = cause.getCause();

        }
         return false;


    }


}
