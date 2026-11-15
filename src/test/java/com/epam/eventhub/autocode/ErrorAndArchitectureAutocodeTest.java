package com.epam.eventhub.autocode;

import com.epam.eventhub.controller.AuthController;
import com.epam.eventhub.controller.EventController;
import com.epam.eventhub.controller.OrganizerController;
import com.epam.eventhub.controller.RegistrationController;
import com.epam.eventhub.model.Role;
import com.epam.eventhub.repository.CategoryRepository;
import com.epam.eventhub.repository.EventRepository;
import com.epam.eventhub.repository.EventSessionRepository;
import com.epam.eventhub.repository.LocationRepository;
import com.epam.eventhub.repository.RegistrationRepository;
import com.epam.eventhub.repository.UserRepository;
import com.epam.eventhub.service.AuthService;
import com.epam.eventhub.service.CatalogService;
import com.epam.eventhub.service.EventService;
import com.epam.eventhub.service.RegistrationService;
import com.epam.eventhub.session.AuthenticatedUser;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class ErrorAndArchitectureAutocodeTest extends EventHubIntegrationTestSupport {

    @Test
    void missingRecordReturnsA404ErrorPage() throws Exception {
        mockMvc.perform(get("/events/999999999"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("status", 404))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void technicalDatabaseFailureReturnsSafe500WithoutSqlDetails() throws Exception {
        jdbc.execute("DROP TABLE events CASCADE");

        var result = mockMvc.perform(get("/events"))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("status", 500))
                .andExpect(model().attributeExists("errorReference"))
                .andReturn();

        String message = String.valueOf(result.getModelAndView().getModel().get("errorMessage"));
        assertThat(message).doesNotContainIgnoringCase("select", "jdbc", "events", "syntax");
    }

    @Test
    void passwordEncoderUsesRandomSaltAndMatchesSafely() {
        String first = passwordEncoder.encode(DEMO_PASSWORD);
        String second = passwordEncoder.encode(DEMO_PASSWORD);

        assertThat(first).isNotEqualTo(second);
        assertThat(passwordEncoder.matches(DEMO_PASSWORD, first)).isTrue();
        assertThat(passwordEncoder.matches("incorrect", first)).isFalse();
        assertThat(passwordEncoder.matches(DEMO_PASSWORD, "not-a-valid-hash")).isFalse();
    }

    @Test
    void requiredContractsAreInterfacesAndHaveSpringImplementations() {
        List<Class<?>> contracts = List.of(
                AuthService.class, CatalogService.class, EventService.class, RegistrationService.class,
                UserRepository.class, CategoryRepository.class, LocationRepository.class,
                EventRepository.class, EventSessionRepository.class, RegistrationRepository.class);

        assertThat(contracts).allMatch(Class::isInterface);
        contracts.forEach(contract -> assertThat(applicationContextBeans(contract))
                .as("Spring implementation for %s", contract.getSimpleName())
                .hasSize(1));
    }

    @Test
    void controllersDoNotDependDirectlyOnRepositories() {
        List<Class<?>> controllers = List.of(
                AuthController.class, EventController.class, OrganizerController.class, RegistrationController.class);
        var fieldTypes = controllers.stream()
                .flatMap(controller -> Arrays.stream(controller.getDeclaredFields()))
                .map(Field::getType)
                .toList();

        assertThat(fieldTypes)
                .noneMatch(type -> type.getPackageName().startsWith("com.epam.eventhub.repository"));
    }

    @Test
    void authenticatedSessionObjectContainsNoPasswordData() {
        List<Field> instanceFields = Arrays.stream(AuthenticatedUser.class.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .toList();

        assertThat(instanceFields).extracting(Field::getName)
                .containsExactlyInAnyOrder("id", "fullName", "email", "role");
        assertThat(instanceFields).extracting(Field::getName)
                .noneMatch(name -> name.toLowerCase().contains("password") || name.toLowerCase().contains("hash"));
    }

    @Test
    void serviceCollectionsCannotBeModifiedByCaller() {
        assertThatThrownBy(() -> eventService.findPublished(null).clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> catalogService.getCategories().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private String[] applicationContextBeans(Class<?> contract) {
        return applicationContext.getBeanNamesForType(contract);
    }

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.context.ApplicationContext applicationContext;

}
