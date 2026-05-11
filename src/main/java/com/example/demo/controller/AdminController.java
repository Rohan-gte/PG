package com.example.demo.controller;

import com.example.demo.service.RoomService;
import com.example.demo.service.TenantService;
import com.example.demo.service.PaymentService;
import com.example.demo.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ComplaintService complaintService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // These would be real counts from repositories in a full implementation
        model.addAttribute("totalTenants", 0);
        model.addAttribute("totalRooms", 0);
        model.addAttribute("pendingComplaints", 0);
        model.addAttribute("monthlyRevenue", 0.0);
        return "admin/dashboard";
    }

    @GetMapping("/rooms")
    public String manageRooms(Model model) {
        model.addAttribute("rooms", roomService.getAvailableRooms());
        return "admin/rooms";
    }

    @GetMapping("/tenants")
    public String manageTenants() {
        return "admin/tenants";
    }

    @GetMapping("/payments")
    public String trackPayments() {
        return "admin/payments";
    }

    @GetMapping("/complaints")
    public String monitorComplaints() {
        return "admin/complaints";
    }

    @GetMapping("/reports")
    public String generateReports() {
        return "admin/reports";
    }

    @GetMapping("/staff")
    public String manageStaff() {
        return "admin/staff";
    }
}
