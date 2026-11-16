package com.epam.eventhub.controller;

import org.springframework.ui.Model;
import com.epam.eventhub.config.SessionAttributes;
import com.epam.eventhub.service.RegistrationService;
import com.epam.eventhub.session.AuthenticatedUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistrationController {
    private final RegistrationService registrationService;
    public RegistrationController(RegistrationService registrationService){
        this.registrationService = registrationService;
    }
    @PostMapping("/sessions/{sessionId}/register")
    public String register(@PathVariable long sessionId,
                           HttpSession session){
        AuthenticatedUser usr = (AuthenticatedUser) session.getAttribute(SessionAttributes.CURRENT_USER);

        long eventId = registrationService.register(sessionId, usr.getId());
        return "redirect:/events/" + eventId;
    }
    @GetMapping("/profile/registrations")
    public String showMyRegistrations(
            HttpSession session,
            Model model
    ){
        AuthenticatedUser user = (AuthenticatedUser) session.getAttribute(SessionAttributes.CURRENT_USER);
        model.addAttribute("registrations",
                registrationService.findForParticipant(user.getId()));
        return "profile/registrations";
    }
    @PostMapping("/registrations/{registrationId}/cancel")
    public String cancelRegistration(
            @PathVariable long  registrationId,
            HttpSession session
    ){
        AuthenticatedUser user = (AuthenticatedUser) session.getAttribute(SessionAttributes.CURRENT_USER);

        registrationService.cancel(registrationId, user.getId());
        return "redirect:/profile/registrations";
    }


}
