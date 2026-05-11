package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/receptionist/dashboard")
    public String receptionistDashboard() {
        return "receptionist/dashboard";
    }

    @GetMapping("/tenant/dashboard")
    public String tenantDashboard() {
        return "tenant/dashboard";
    }
}
