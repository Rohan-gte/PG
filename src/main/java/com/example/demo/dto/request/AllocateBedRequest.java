package com.example.demo.dto.request;

import jakarta.validation.constraints.NotNull;

public class AllocateBedRequest {
    @NotNull
    private Long bookingRequestId;

    @NotNull
    private Long bedId;

    public Long getBookingRequestId() { return bookingRequestId; }
    public void setBookingRequestId(Long bookingRequestId) { this.bookingRequestId = bookingRequestId; }
    public Long getBedId() { return bedId; }
    public void setBedId(Long bedId) { this.bedId = bedId; }
}
