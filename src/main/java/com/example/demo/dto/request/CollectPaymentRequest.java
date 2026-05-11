package com.example.demo.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class CollectPaymentRequest {
    @NotNull
    private Long paymentId;

    @NotNull @DecimalMin("0.0")
    private BigDecimal amount;

    @Size(max = 60)
    private String paymentMethod;

    @Size(max = 500)
    private String notes;

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
