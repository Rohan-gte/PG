package com.example.demo.controller;

import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.RegisterOwnerRequest;
import com.example.demo.dto.request.RegisterTenantRequest;
import com.example.demo.dto.response.AuthResponse;
import com.example.demo.dto.response.UserDto;
import com.example.demo.security.CurrentUser;
import com.example.demo.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CurrentUser currentUser;

    public AuthController(AuthService authService, CurrentUser currentUser) {
        this.authService = authService;
        this.currentUser = currentUser;
    }

    @PostMapping("/register/tenant")
    public UserDto registerTenant(@Valid @RequestBody RegisterTenantRequest req) {
        return authService.registerTenant(req);
    }

    @PostMapping("/register/owner")
    public UserDto registerOwner(@Valid @RequestBody RegisterOwnerRequest req) {
        return authService.registerOwner(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/me")
    public UserDto me() {
        return authService.me(currentUser.id());
    }
}
