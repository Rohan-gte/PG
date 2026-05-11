package com.example.demo.controller;

import com.example.demo.dto.request.BookingRequestDto;
import com.example.demo.dto.response.ApiMessage;
import com.example.demo.dto.response.BookingRequestResponseDto;
import com.example.demo.dto.response.DashboardDto;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.dto.response.PaymentDto;
import com.example.demo.security.CurrentUser;
import com.example.demo.service.AnalyticsService;
import com.example.demo.service.BookingService;
import com.example.demo.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenant")
public class TenantController {

    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final AnalyticsService analyticsService;
    private final CurrentUser currentUser;

    public TenantController(BookingService bookingService,
                            PaymentService paymentService,
                            AnalyticsService analyticsService,
                            CurrentUser currentUser) {
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.analyticsService = analyticsService;
        this.currentUser = currentUser;
    }

    @GetMapping("/dashboard")
    public DashboardDto dashboard() {
        return analyticsService.tenantDashboard(currentUser.id());
    }

    @PostMapping("/bookings")
    public BookingRequestResponseDto submitBooking(@Valid @RequestBody BookingRequestDto req) {
        return bookingService.submit(currentUser.id(), req);
    }

    @GetMapping("/bookings")
    public PageResponse<BookingRequestResponseDto> myBookings(Pageable pageable) {
        return bookingService.listForTenant(currentUser.id(), pageable);
    }

    @PostMapping("/bookings/{id}/cancel")
    public ApiMessage cancel(@PathVariable Long id) {
        return bookingService.cancel(currentUser.id(), id);
    }

    @GetMapping("/payments")
    public List<PaymentDto> payments() {
        return paymentService.listForTenant(currentUser.id());
    }

    @PostMapping("/payments/{id}/pay")
    public PaymentDto pay(@PathVariable Long id) {
        return paymentService.tenantPay(currentUser.id(), id);
    }
}
