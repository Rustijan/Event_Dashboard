package com.epam.eventhub.acceptance;

import com.epam.eventhub.config.SessionAttributes;
import com.epam.eventhub.exception.AuthenticationException;
import com.epam.eventhub.exception.BusinessRuleException;
import com.epam.eventhub.model.Role;
import com.epam.eventhub.session.AuthenticatedUser;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class AuthenticationAndAccessTest extends EventHubIntegrationTestSupport {

    @Test
    void demoParticipantCanLogInAndWrongPasswordIsRejected() throws Exception {
        var session = login("  PARTICIPANT@EVENTHUB.LOCAL ", DEMO_PASSWORD);
        assertThat(session).isNotNull();
        AuthenticatedUser user = (AuthenticatedUser) session.getAttribute(SessionAttributes.CURRENT_USER);
        assertThat(user.getEmail()).isEqualTo(PARTICIPANT_EMAIL);
        assertThat(user.getRole()).isEqualTo(Role.PARTICIPANT);

        mockMvc.perform(post("/login")
                        .param("email", PARTICIPANT_EMAIL)
                        .param("password", "wrong-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attributeExists("loginError"));

        assertThatThrownBy(() -> authService.authenticate(PARTICIPANT_EMAIL, "wrong-password"))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void registrationNormalizesEmailHashesPasswordAndRejectsDuplicate() throws Exception {
        mockMvc.perform(post("/register")
                        .param("fullName", "  New Participant  ")
                        .param("email", "  New.User@Example.COM ")
                        .param("password", DEMO_PASSWORD)
                        .param("role", "PARTICIPANT"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events"));

        String hash = jdbc.queryForObject(
                "SELECT password_hash FROM users WHERE email = 'new.user@example.com'", String.class);
        assertThat(hash).isNotEqualTo(DEMO_PASSWORD);
        assertThat(passwordEncoder.matches(DEMO_PASSWORD, hash)).isTrue();

        mockMvc.perform(post("/register")
                        .param("fullName", "Duplicate")
                        .param("email", "NEW.USER@EXAMPLE.COM")
                        .param("password", DEMO_PASSWORD)
                        .param("role", "PARTICIPANT"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("registrationError"));

        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM users WHERE LOWER(email) = 'new.user@example.com'", Integer.class))
                .isEqualTo(1);
        assertThatThrownBy(() -> registerUser("Duplicate", "NEW.USER@EXAMPLE.COM", Role.PARTICIPANT))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void privateRoutesEnforceAuthenticationAndRoleInBothDirections() throws Exception {
        mockMvc.perform(get("/profile/registrations"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        mockMvc.perform(get("/organizer/events").session(login(PARTICIPANT_EMAIL, DEMO_PASSWORD)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/profile/registrations").session(login(ORGANIZER_EMAIL, DEMO_PASSWORD)))
                .andExpect(status().isForbidden());
    }
}
