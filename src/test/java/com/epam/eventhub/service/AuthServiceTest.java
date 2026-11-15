package com.epam.eventhub.service;

import com.epam.eventhub.dto.RegistrationForm;
import com.epam.eventhub.exception.AuthenticationException;
import com.epam.eventhub.exception.BusinessRuleException;
import com.epam.eventhub.exception.DataAccessException;
import com.epam.eventhub.model.Role;
import com.epam.eventhub.model.User;
import com.epam.eventhub.repository.UserRepository;
import com.epam.eventhub.service.impl.AuthServiceImpl;
import com.epam.eventhub.session.AuthenticatedUser;
import com.epam.eventhub.util.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        authService = new AuthServiceImpl(userRepository, passwordEncoder);
    }

    @Test
    void successfulAuthenticationReturnsPasswordFreeSessionUser() {
        User user = new User(7L, "Olivia", "olivia@example.com", "stored-hash", Role.ORGANIZER);
        when(userRepository.findByEmail("olivia@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "stored-hash")).thenReturn(true);

        AuthenticatedUser authenticated = authService.authenticate(" Olivia@Example.com ", "Password123!");

        assertEquals(7L, authenticated.getId());
        assertEquals("Olivia", authenticated.getFullName());
        assertEquals(Role.ORGANIZER, authenticated.getRole());
    }

    @Test
    void invalidPasswordThrowsAuthenticationException() {
        User user = new User(7L, "Olivia", "olivia@example.com", "stored-hash", Role.ORGANIZER);
        when(userRepository.findByEmail("olivia@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "stored-hash")).thenReturn(false);

        assertThrows(AuthenticationException.class,
                () -> authService.authenticate("olivia@example.com", "wrong"));
    }

    @Test
    void registrationRejectsExistingEmail() {
        RegistrationForm form = registrationForm();
        when(userRepository.existsByEmail("participant@example.com")).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> authService.register(form));
    }

    @Test
    void uniqueConstraintRaceIsTranslatedToBusinessException() {
        RegistrationForm form = registrationForm();
        when(userRepository.existsByEmail("participant@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("hash");
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataAccessException("save user", new SQLException("duplicate", "23505")));

        assertThrows(BusinessRuleException.class, () -> authService.register(form));
    }

    @Test
    void registrationReturnsMinimalAuthenticatedUser() {
        RegistrationForm form = registrationForm();
        when(passwordEncoder.encode("Password123!")).thenReturn("hash");
        when(userRepository.save(any(User.class)))
                .thenReturn(new User(11L, "Peter Participant", "participant@example.com", "hash", Role.PARTICIPANT));

        AuthenticatedUser registered = authService.register(form);

        assertEquals(11L, registered.getId());
        assertEquals("participant@example.com", registered.getEmail());
        verify(passwordEncoder).encode("Password123!");
    }

    private RegistrationForm registrationForm() {
        RegistrationForm form = new RegistrationForm();
        form.setFullName("Peter Participant");
        form.setEmail("Participant@Example.com");
        form.setPassword("Password123!");
        form.setRole(Role.PARTICIPANT);
        return form;
    }
}
