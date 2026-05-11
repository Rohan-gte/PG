package com.example.demo.security;

import com.example.demo.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    public AuthUser get() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || a.getPrincipal() == null || !(a.getPrincipal() instanceof AuthUser)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return (AuthUser) a.getPrincipal();
    }

    public Long id() {
        return get().getId();
    }

    public String role() {
        return get().getRole();
    }

    public String email() {
        return get().getEmail();
    }
}
