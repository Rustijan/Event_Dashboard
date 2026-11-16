package com.epam.eventhub.controller;


import com.epam.eventhub.config.SessionAttributes;
import com.epam.eventhub.dto.LoginForm;
import com.epam.eventhub.dto.RegistrationForm;
import com.epam.eventhub.exception.BusinessRuleException;
import com.epam.eventhub.model.Role;
import com.epam.eventhub.service.AuthService;
import com.epam.eventhub.session.AuthenticatedUser;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {


    private final AuthService authService;
    public AuthController(AuthService authService){
        this.authService = authService;
    }


    @GetMapping("/register")
    public String showRegistrationForm(Model model){
        model.addAttribute("registrationForm", new RegistrationForm());

        model.addAttribute("roles", Role.values());


        return "auth/register";
    }


    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registrationForm")
            RegistrationForm registrationForm,
            BindingResult bindingResult,
            Model model

    ){
        if(bindingResult.hasErrors()){
            model.addAttribute("roles", Role.values());
            return "auth/register";
        }
        try{
            authService.register(registrationForm);
            return "redirect:/login";
        }catch(BusinessRuleException e){
            model.addAttribute("roles", Role.values());
            model.addAttribute("registrationError", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(Model model){
        model.addAttribute("loginForm", new LoginForm());
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(
            @Valid @ModelAttribute("loginForm") LoginForm loginForm,
            BindingResult bindingResult,
            Model model,
            HttpSession session
    ){
        if(bindingResult.hasErrors()){
            return "auth/login";
        }
        try{
            AuthenticatedUser authenticatedUser = authService.authenticate(
                    loginForm.getEmail(),
                    loginForm.getPassword()
            );
            session.setAttribute(
                    SessionAttributes.CURRENT_USER,
                    authenticatedUser
            );
            return "redirect:/events";
        }catch(BusinessRuleException e){
            model.addAttribute("loginError", e.getMessage());
            return "auth/login";
        }
    }
    @PostMapping("/logout")
    public String logout(HttpSession session){
        session.invalidate();
        return "redirect:/events";
    }




}
