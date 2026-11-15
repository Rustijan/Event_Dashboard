package com.epam.eventhub.acceptance;

import com.epam.eventhub.dto.EventForm;
import com.epam.eventhub.dto.RegistrationForm;
import com.epam.eventhub.dto.SessionForm;
import com.epam.eventhub.model.Event;
import com.epam.eventhub.model.EventSession;
import com.epam.eventhub.model.Role;
import com.epam.eventhub.service.AuthService;
import com.epam.eventhub.service.CatalogService;
import com.epam.eventhub.service.EventService;
import com.epam.eventhub.service.RegistrationService;
import com.epam.eventhub.util.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = {"/test-schema.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
abstract class EventHubIntegrationTestSupport {
    protected static final String ORGANIZER_EMAIL = "organizer@eventhub.local";
    protected static final String PARTICIPANT_EMAIL = "participant@eventhub.local";
    protected static final String DEMO_PASSWORD = "Password123!";

    @Autowired protected MockMvc mockMvc;
    @Autowired protected JdbcTemplate jdbc;
    @Autowired protected AuthService authService;
    @Autowired protected CatalogService catalogService;
    @Autowired protected EventService eventService;
    @Autowired protected RegistrationService registrationService;
    @Autowired protected PasswordEncoder passwordEncoder;

    @BeforeEach
    void verifyRelativeDataset() {
        LocalDateTime earliest = jdbc.queryForObject(
                "SELECT MIN(starts_at) FROM event_sessions", LocalDateTime.class);
        if (earliest == null || !earliest.isAfter(LocalDateTime.now().plusDays(6))) {
            throw new AssertionError("Supplied test data must be generated relative to the current test run");
        }
    }

    protected long id(String sql, Object... arguments) {
        Long value = jdbc.queryForObject(sql, Long.class, arguments);
        if (value == null) {
            throw new AssertionError("Expected query to return an ID: " + sql);
        }
        return value;
    }

    protected MockHttpSession login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/login")
                        .param("email", email)
                        .param("password", password))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    protected long registerUser(String name, String email, Role role) {
        RegistrationForm form = new RegistrationForm();
        form.setFullName(name);
        form.setEmail(email);
        form.setPassword(DEMO_PASSWORD);
        form.setRole(role);
        return authService.register(form).getId();
    }

    protected Event createDraft(long organizerId, String title) {
        EventForm form = new EventForm();
        form.setTitle(title);
        form.setDescription("A sufficiently detailed description used by the open acceptance tests.");
        form.setCategoryId(id("SELECT id FROM categories WHERE name = 'Technology'"));
        form.setLocationId(id("SELECT id FROM locations WHERE name = 'Community Room'"));
        return eventService.create(form, organizerId);
    }

    protected EventSession addFutureSession(long eventId, long organizerId, int capacity) {
        SessionForm form = new SessionForm();
        form.setStartsAt(LocalDateTime.now().plusDays(30).withNano(0));
        form.setEndsAt(LocalDateTime.now().plusDays(30).plusHours(2).withNano(0));
        form.setCapacity(capacity);
        return eventService.addSession(eventId, form, organizerId);
    }
}
