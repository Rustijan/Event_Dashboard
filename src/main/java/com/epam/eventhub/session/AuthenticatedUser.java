package com.epam.eventhub.session;

import com.epam.eventhub.model.Role;

public class AuthenticatedUser {
    private final Long id;
    private final String fullName;
    private final String email;
    private final Role role;

    public AuthenticatedUser(Long id, String fullName, String email, Role role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }
    public Long getId() {
        return id;
    }
    public String getFullName() {
        return fullName;
    }
    public String getEmail() {
        return email;
    }
    public Role getRole() {
        return role;
    }

}
