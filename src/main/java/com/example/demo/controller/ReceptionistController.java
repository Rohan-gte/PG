package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/receptionist")
public class ReceptionistController {

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("activeTenants", 0);
        model.addAttribute("availableBeds", 0);
        model.addAttribute("todayVisitors", 0);
        model.addAttribute("pendingComplaints", 0);
        return "receptionist/dashboard";
    }

    @GetMapping("/registration")
    public String registerTenant() {
        return "receptionist/registration";
    }

    @GetMapping("/availability")
    public String checkAvailability() {
        return "receptionist/availability";
    }

    @GetMapping("/payments")
    public String paymentCollection() {
        return "receptionist/payments";
    }

    @GetMapping("/visitors")
    public String manageVisitors() {
        return "receptionist/visitors";
    }

    @GetMapping("/complaints")
    public String handleComplaints() {
        return "receptionist/complaints";
    }
}
