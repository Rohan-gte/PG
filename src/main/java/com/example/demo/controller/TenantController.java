package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/tenant")
public class TenantController {

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("rentStatus", "PAID");
        model.addAttribute("roomNumber", "A-101");
        model.addAttribute("pendingComplaints", 0);
        return "tenant/dashboard";
    }

    @GetMapping("/payments")
    public String viewPayments() {
        return "tenant/payments";
    }

    @GetMapping("/food")
    public String viewFoodMenu() {
        return "tenant/food";
    }

    @GetMapping("/complaints")
    public String raiseComplaint() {
        return "tenant/complaints";
    }

    @GetMapping("/visitors")
    public String approveVisitors() {
        return "tenant/visitors";
    }
}
