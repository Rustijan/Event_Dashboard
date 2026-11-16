package com.epam.eventhub.controller;

import com.epam.eventhub.config.SessionAttributes;
import com.epam.eventhub.dto.EventSearchCriteria;
import com.epam.eventhub.model.Event;
import com.epam.eventhub.service.CatalogService;
import com.epam.eventhub.service.EventService;
import com.epam.eventhub.session.AuthenticatedUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
@Controller
public class EventController {
    private final EventService eventService;
    private final CatalogService catalogService;

    public EventController(EventService eventService, CatalogService catalogService) {
        this.eventService = eventService;
        this.catalogService = catalogService;
    }

    @GetMapping({"/", "/events"})
    public String showEvents(
            @ModelAttribute("criteria") EventSearchCriteria criteria,
            Model model
            ){
        model.addAttribute(
                "events",
                eventService.findPublished(criteria)
        );
        model.addAttribute(
                "categories",
                catalogService.getCategories()
        );
        model.addAttribute(
                "cities",
                catalogService.getCities()
        );
        return "events/list";
    }
    @GetMapping("/events/{eventId}")
    public String showEventDetails(
            @PathVariable long eventId,
            Model model,
            HttpSession session
    ){
        Event event = eventService.getById(eventId);
        model.addAttribute("event", event);
        model.addAttribute("sessions", eventService.getSessions(eventId));

        AuthenticatedUser currentUser = (AuthenticatedUser)
                session.getAttribute(SessionAttributes.CURRENT_USER);
        model.addAttribute("currentUser", currentUser);
        return "events/details";

    }
}
