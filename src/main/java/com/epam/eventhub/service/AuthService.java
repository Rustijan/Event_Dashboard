package com.epam.eventhub.service;

import com.epam.eventhub.dto.RegistrationForm;
import com.epam.eventhub.session.AuthenticatedUser;

public interface AuthService {
    AuthenticatedUser authenticate(String email, String password);
    AuthenticatedUser register(RegistrationForm registrationForm);


}
