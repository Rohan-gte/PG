package com.example.demo.security;

import java.io.Serializable;

public class AuthUser implements Serializable {
    private final Long id;
    private final String email;
    private final String role;

    public AuthUser(Long id, String email, String role) {
        this.id = id;
        this.email = email;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getRole() { return role; }

    @Override
    public String toString() {
        return email;
    }
}
