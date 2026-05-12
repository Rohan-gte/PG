package com.example.demo.controller;

import com.example.demo.dto.request.AddRoomRequest;
import com.example.demo.dto.request.CreateBuildingRequest;
import com.example.demo.dto.request.UpdateBuildingRequest;
import com.example.demo.dto.request.UpdateRoomRequest;
import com.example.demo.dto.response.ApiMessage;
import com.example.demo.dto.response.BuildingDto;
import com.example.demo.dto.response.DashboardDto;
import com.example.demo.dto.response.RoomDto;
import com.example.demo.security.CurrentUser;
import com.example.demo.service.AnalyticsService;
import com.example.demo.service.BuildingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/owner")
public class OwnerController {

    private final BuildingService buildingService;
    private final AnalyticsService analyticsService;
    private final CurrentUser currentUser;

    public OwnerController(BuildingService buildingService,
                           AnalyticsService analyticsService,
                           CurrentUser currentUser) {
        this.buildingService = buildingService;
        this.analyticsService = analyticsService;
        this.currentUser = currentUser;
    }

    @GetMapping("/dashboard")
    public DashboardDto dashboard() {
        return analyticsService.ownerDashboard(currentUser.id());
    }

    @GetMapping("/buildings")
    public List<BuildingDto> myBuildings() {
        return buildingService.listForOwner(currentUser.id());
    }

    @PostMapping("/buildings")
    public BuildingDto createBuilding(@Valid @RequestBody CreateBuildingRequest req) {
        return buildingService.createBuilding(currentUser.id(), req);
    }

    @GetMapping("/buildings/{id:\\d+}")
    public BuildingDto getBuilding(@PathVariable Long id) {
        return buildingService.getForOwner(currentUser.id(), id);
    }

    @PutMapping("/buildings/{id:\\d+}")
    public BuildingDto updateBuilding(@PathVariable Long id, @Valid @RequestBody UpdateBuildingRequest req) {
        return buildingService.updateBuilding(currentUser.id(), id, req);
    }

    @DeleteMapping("/buildings/{id:\\d+}")
    public ApiMessage deleteBuilding(@PathVariable Long id) {
        return buildingService.deleteBuilding(currentUser.id(), id);
    }

    @GetMapping("/buildings/{id:\\d+}/rooms")
    public List<RoomDto> rooms(@PathVariable Long id) {
        return buildingService.roomsForOwner(currentUser.id(), id);
    }

    @PostMapping("/buildings/{id:\\d+}/rooms")
    public RoomDto addRoom(@PathVariable Long id, @Valid @RequestBody AddRoomRequest req) {
        return buildingService.addRoom(currentUser.id(), id, req);
    }

    @PutMapping("/rooms/{roomId}")
    public RoomDto updateRoom(@PathVariable Long roomId, @Valid @RequestBody UpdateRoomRequest req) {
        return buildingService.updateRoom(currentUser.id(), roomId, req);
    }

    @DeleteMapping("/rooms/{roomId}")
    public ApiMessage deleteRoom(@PathVariable Long roomId) {
        return buildingService.deleteRoom(currentUser.id(), roomId);
    }
}
