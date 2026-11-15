package com.epam.eventhub.autocode;

import com.epam.eventhub.dto.EventSearchCriteria;
import com.epam.eventhub.model.Event;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class ApplicationAndDatasetAutocodeTest extends EventHubIntegrationTestSupport {

    @Test
    void fullApplicationStartsWithTheSuppliedLogicalDataset() throws Exception {
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class)).isEqualTo(2);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM categories", Integer.class)).isEqualTo(5);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM locations", Integer.class)).isEqualTo(3);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM events", Integer.class)).isEqualTo(3);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM event_sessions", Integer.class)).isEqualTo(4);

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(view().name("events/list"))
                .andExpect(model().attributeExists("events", "categories", "cities", "criteria"));
    }

    @Test
    void catalogReturnsOnlyPublishedEventsAndAppliesAllFilters() {
        List<Event> allPublished = eventService.findPublished(new EventSearchCriteria());
        assertThat(allPublished).extracting(Event::getTitle)
                .containsExactlyInAnyOrder("Java Community Evening", "Clean Code Workshop");

        EventSearchCriteria text = new EventSearchCriteria();
        text.setQuery("naming");
        assertThat(eventService.findPublished(text)).extracting(Event::getTitle)
                .containsExactly("Clean Code Workshop");

        EventSearchCriteria category = new EventSearchCriteria();
        category.setCategoryId(id("SELECT id FROM categories WHERE name = 'Technology'"));
        category.setCity("mInSk");
        assertThat(eventService.findPublished(category)).extracting(Event::getTitle)
                .containsExactly("Java Community Evening");
    }

    @Test
    void catalogsAreLoadedFromTheDatabase() {
        assertThat(catalogService.getCategories()).hasSize(5);
        assertThat(catalogService.getLocations()).hasSize(3);
        assertThat(catalogService.getCities()).containsExactly("Minsk");
    }
}
