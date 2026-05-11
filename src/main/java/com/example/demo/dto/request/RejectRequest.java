package com.example.demo.dto.request;

import jakarta.validation.constraints.Size;

public class RejectRequest {
    @Size(max = 500)
    private String reason;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
