package com.example.demo.controller;

import com.example.demo.dto.response.AvailabilityDto;
import com.example.demo.dto.response.BuildingDto;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.enums.SharingType;
import com.example.demo.service.AnalyticsService;
import com.example.demo.service.BuildingBrowseService;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/public")
public class BuildingPublicController {

    private final BuildingBrowseService browseService;
    private final AnalyticsService analyticsService;

    public BuildingPublicController(BuildingBrowseService browseService, AnalyticsService analyticsService) {
        this.browseService = browseService;
        this.analyticsService = analyticsService;
    }

    @GetMapping("/buildings")
    public PageResponse<BuildingDto> list(@RequestParam(required = false) String q,
                                          @RequestParam(required = false) String city,
                                          @RequestParam(required = false) String area,
                                          @RequestParam(required = false) SharingType sharingType,
                                          @RequestParam(required = false) BigDecimal maxRent,
                                          @RequestParam(required = false) Boolean availableOnly,
                                          Pageable pageable) {
        return browseService.search(q, city, area, sharingType, maxRent, availableOnly, pageable);
    }

    @GetMapping("/buildings/{id}")
    public BuildingDto get(@PathVariable Long id) {
        return browseService.getById(id);
    }

    @GetMapping("/buildings/{id}/availability")
    public AvailabilityDto availability(@PathVariable Long id) {
        return analyticsService.availabilityForBuilding(id);
    }
}
