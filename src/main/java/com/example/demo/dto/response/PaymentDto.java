package com.example.demo.dto.response;

import com.example.demo.entity.Payment;
import com.example.demo.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public class PaymentDto {
    private Long id;
    private Long tenantUserId;
    private String tenantName;
    private String tenantEmail;
    private Long buildingId;
    private String buildingName;
    private Long bedId;
    private String bedNumber;
    private String roomNumber;
    private String monthYear;
    private BigDecimal amount;
    private PaymentStatus status;
    private Instant paidAt;
    private String receiptNumber;
    private String paymentMethod;
    private String notes;
    private Instant createdAt;

    public static PaymentDto of(Payment p) {
        PaymentDto d = new PaymentDto();
        d.id = p.getId();
        d.tenantUserId = p.getTenantUserId();
        d.buildingId = p.getBuildingId();
        d.bedId = p.getBedId();
        d.monthYear = p.getMonthYear();
        d.amount = p.getAmount();
        d.status = p.getStatus();
        d.paidAt = p.getPaidAt();
        d.receiptNumber = p.getReceiptNumber();
        d.paymentMethod = p.getPaymentMethod();
        d.notes = p.getNotes();
        d.createdAt = p.getCreatedAt();
        return d;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantUserId() { return tenantUserId; }
    public void setTenantUserId(Long tenantUserId) { this.tenantUserId = tenantUserId; }
    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }
    public String getTenantEmail() { return tenantEmail; }
    public void setTenantEmail(String tenantEmail) { this.tenantEmail = tenantEmail; }
    public Long getBuildingId() { return buildingId; }
    public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }
    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }
    public Long getBedId() { return bedId; }
    public void setBedId(Long bedId) { this.bedId = bedId; }
    public String getBedNumber() { return bedNumber; }
    public void setBedNumber(String bedNumber) { this.bedNumber = bedNumber; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getMonthYear() { return monthYear; }
    public void setMonthYear(String monthYear) { this.monthYear = monthYear; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
