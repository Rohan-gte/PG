package com.example.demo.controller;

import com.example.demo.dto.request.AllocateBedRequest;
import com.example.demo.dto.request.CollectPaymentRequest;
import com.example.demo.dto.request.RejectRequest;
import com.example.demo.dto.response.ApiMessage;
import com.example.demo.dto.response.BedDto;
import com.example.demo.dto.response.BookingRequestResponseDto;
import com.example.demo.dto.response.BuildingDto;
import com.example.demo.dto.response.DashboardDto;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.dto.response.PaymentDto;
import com.example.demo.dto.response.RoomDto;
import com.example.demo.dto.response.UserDto;
import com.example.demo.enums.BookingStatus;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.enums.SharingType;
import com.example.demo.security.CurrentUser;
import com.example.demo.service.AllocationService;
import com.example.demo.service.AnalyticsService;
import com.example.demo.service.BookingService;
import com.example.demo.service.PaymentService;
import com.example.demo.service.ReceptionistService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/receptionist")
public class ReceptionistController {

    private final ReceptionistService receptionistService;
    private final BookingService bookingService;
    private final AllocationService allocationService;
    private final PaymentService paymentService;
    private final AnalyticsService analyticsService;
    private final CurrentUser currentUser;

    public ReceptionistController(ReceptionistService receptionistService,
                                  BookingService bookingService,
                                  AllocationService allocationService,
                                  PaymentService paymentService,
                                  AnalyticsService analyticsService,
                                  CurrentUser currentUser) {
        this.receptionistService = receptionistService;
        this.bookingService = bookingService;
        this.allocationService = allocationService;
        this.paymentService = paymentService;
        this.analyticsService = analyticsService;
        this.currentUser = currentUser;
    }

    @GetMapping("/dashboard")
    public DashboardDto dashboard() {
        return analyticsService.receptionistDashboard(currentUser.id());
    }

    @GetMapping("/building")
    public BuildingDto myBuilding() {
        return receptionistService.myBuilding(currentUser.id());
    }

    @GetMapping("/rooms")
    public List<RoomDto> rooms() {
        return receptionistService.rooms(currentUser.id());
    }

    @GetMapping("/tenants")
    public List<UserDto> tenants() {
        return receptionistService.tenants(currentUser.id());
    }

    @GetMapping("/requests")
    public PageResponse<BookingRequestResponseDto> requests(@RequestParam(required = false) BookingStatus status,
                                                            Pageable pageable) {
        return bookingService.listForReceptionist(currentUser.id(), status, pageable);
    }

    @PostMapping("/requests/{id}/approve")
    public BookingRequestResponseDto approve(@PathVariable Long id) {
        return bookingService.approve(currentUser.id(), id);
    }

    @PostMapping("/requests/{id}/reject")
    public BookingRequestResponseDto reject(@PathVariable Long id, @RequestBody(required = false) RejectRequest body) {
        return bookingService.reject(currentUser.id(), id, body == null ? null : body.getReason());
    }

    @GetMapping("/available-beds")
    public List<BedDto> availableBeds(@RequestParam Long buildingId, @RequestParam SharingType sharingType) {
        return allocationService.availableBedsForSharing(currentUser.id(), buildingId, sharingType);
    }

    @PostMapping("/allocate")
    public BookingRequestResponseDto allocate(@Valid @RequestBody AllocateBedRequest req) {
        return allocationService.allocate(currentUser.id(), req.getBookingRequestId(), req.getBedId());
    }

    @PostMapping("/checkout/{tenantId}")
    public ApiMessage checkout(@PathVariable Long tenantId) {
        return allocationService.checkout(currentUser.id(), tenantId);
    }

    @GetMapping("/payments")
    public PageResponse<PaymentDto> payments(@RequestParam(required = false) PaymentStatus status,
                                             Pageable pageable) {
        return paymentService.listForReceptionist(currentUser.id(), status, pageable);
    }

    @PostMapping("/payments/collect")
    public PaymentDto collect(@Valid @RequestBody CollectPaymentRequest req) {
        return paymentService.collect(currentUser.id(), req);
    }

    @PostMapping("/payments/generate")
    public ApiMessage generate(@RequestParam(required = false) String monthYear) {
        return paymentService.generateMonth(currentUser.id(), monthYear);
    }
}
