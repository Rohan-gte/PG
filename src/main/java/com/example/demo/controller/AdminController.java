package com.example.demo.controller;

import com.example.demo.dto.request.RejectRequest;
import com.example.demo.dto.response.ApiMessage;
import com.example.demo.dto.response.BuildingDto;
import com.example.demo.dto.response.DashboardDto;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.dto.response.UserDto;
import com.example.demo.enums.OwnerStatus;
import com.example.demo.enums.Role;
import com.example.demo.service.AdminService;
import com.example.demo.service.AnalyticsService;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final AnalyticsService analyticsService;

    public AdminController(AdminService adminService, AnalyticsService analyticsService) {
        this.adminService = adminService;
        this.analyticsService = analyticsService;
    }

    @GetMapping("/owners")
    public PageResponse<UserDto> listOwners(@RequestParam(required = false) OwnerStatus status,
                                            Pageable pageable) {
        return adminService.listOwners(status, pageable);
    }

    @PostMapping("/owners/{id}/approve")
    public ApiMessage approveOwner(@PathVariable Long id) {
        return adminService.approveOwner(id);
    }

    @PostMapping("/owners/{id}/reject")
    public ApiMessage rejectOwner(@PathVariable Long id, @RequestBody(required = false) RejectRequest body) {
        return adminService.rejectOwner(id, body == null ? null : body.getReason());
    }

    @GetMapping("/buildings")
    public PageResponse<BuildingDto> listBuildings(@RequestParam(required = false) String q,
                                                   Pageable pageable) {
        return adminService.listBuildings(q, pageable);
    }

    @GetMapping("/tenants")
    public PageResponse<UserDto> listTenants(@RequestParam(required = false) String q, Pageable pageable) {
        return adminService.listByRole(Role.TENANT, q, pageable);
    }

    @GetMapping("/receptionists")
    public PageResponse<UserDto> listReceptionists(@RequestParam(required = false) String q, Pageable pageable) {
        return adminService.listByRole(Role.RECEPTIONIST, q, pageable);
    }

    @GetMapping("/dashboard")
    public DashboardDto dashboard() {
        return analyticsService.adminDashboard();
    }
}
