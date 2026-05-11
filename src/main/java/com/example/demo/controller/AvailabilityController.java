package com.example.demo.controller;

import com.example.demo.dto.response.AvailabilityDto;
import com.example.demo.service.AnalyticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/availability")
@PreAuthorize("isAuthenticated()")
public class AvailabilityController {

    private final AnalyticsService analyticsService;

    public AvailabilityController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/buildings/{id}")
    public AvailabilityDto building(@PathVariable Long id) {
        return analyticsService.availabilityForBuilding(id);
    }
}
