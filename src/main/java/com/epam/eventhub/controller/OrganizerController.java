package com.epam.eventhub.controller;

import com.epam.eventhub.config.SessionAttributes;
import com.epam.eventhub.dto.EventForm;
import com.epam.eventhub.exception.BusinessRuleException;
import com.epam.eventhub.model.Event;
import com.epam.eventhub.service.CatalogService;
import com.epam.eventhub.service.EventService;
import com.epam.eventhub.service.RegistrationService;
import com.epam.eventhub.session.AuthenticatedUser;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class OrganizerController {

    private final EventService eventService;
    private final RegistrationService registrationService;
    private final CatalogService catalogService;

    public OrganizerController(EventService eventService,
                               RegistrationService registrationService,
                               CatalogService catalogService) {
        this.eventService = eventService;
        this.registrationService = registrationService;
        this.catalogService = catalogService;
    }

    @GetMapping("/organizer/events")
    public String showOrganizerEvents(
            HttpSession session,
            Model model
    ){
        AuthenticatedUser authenticatedUser = (AuthenticatedUser)
                session.getAttribute(SessionAttributes.CURRENT_USER);

        model.addAttribute("events",
                            eventService.findForOrganizer(authenticatedUser.getId()));
        return "organizer/events";
    }

    @GetMapping("/organizer/events/create")
    public String showCreateEventForm(Model model){
        model.addAttribute("eventForm", new EventForm());
        model.addAttribute("categories",catalogService.getCategories());
        model.addAttribute("locations", catalogService.getLocations());
        model.addAttribute("pageTitle", "Create Event");
        model.addAttribute("formAction","/organizer/events/create");
        return "organizer/event-form";
    }


    @PostMapping("/organizer/events/create")
    public String createEvent(
            @Valid @ModelAttribute("eventForm") EventForm eventForm,
            BindingResult bindingResult,
            HttpSession session,
            Model model
    ){
        AuthenticatedUser user = (AuthenticatedUser) session.getAttribute(SessionAttributes.CURRENT_USER);

        if(bindingResult.hasErrors()){
            model.addAttribute("categories", catalogService.getCategories());
            model.addAttribute("locations", catalogService.getLocations());
            model.addAttribute("pageTitle", "Create Event");
            model.addAttribute("formAction","/organizer/events/create");
            return "organizer/event-form";
        }
        try {
            eventService.create(eventForm, user.getId());
            return "redirect:/organizer/events";

        } catch (BusinessRuleException e) {

            model.addAttribute("categories", catalogService.getCategories());
            model.addAttribute("locations", catalogService.getLocations());
            model.addAttribute("pageTitle", "Create Event");
            model.addAttribute("formAction", "/organizer/events/create");
            model.addAttribute("eventError", e.getMessage());

            return "organizer/event-form";
        }
    }
    @GetMapping("/organizer/events/{eventId}/edit")
    public String showEditEventForm(
            @PathVariable long eventId,
            HttpSession session,
            Model model
    ){
        AuthenticatedUser authenticatedUser = (AuthenticatedUser)
                session.getAttribute(SessionAttributes.CURRENT_USER);

        Event event = eventService.requireOwner(eventId, authenticatedUser.getId());

        EventForm eventForm = new EventForm();
        eventForm.setTitle(event.getTitle());
        eventForm.setDescription(event.getDescription());
        eventForm.setCategoryId(event.getCategory().getId());
        eventForm.setLocationId(event.getLocation().getId());


        model.addAttribute("eventForm", eventForm);
        model.addAttribute("categories", catalogService.getCategories());
        model.addAttribute("locations", catalogService.getLocations());
        model.addAttribute("pageTitle", "Edit Event");
        model.addAttribute("formAction",
                "/organizer/events/" + eventId + "/edit");

        return "organizer/event-form";

    }



}
