package com.example.demo.entity;

import com.example.demo.enums.PaymentStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments", uniqueConstraints = {
        @UniqueConstraint(name = "uk_payment_tenant_month", columnNames = {"tenant_user_id", "month_year"}),
        @UniqueConstraint(name = "uk_payment_receipt", columnNames = "receipt_number")
}, indexes = {
        @Index(name = "ix_payments_tenant", columnList = "tenant_user_id"),
        @Index(name = "ix_payments_building", columnList = "building_id"),
        @Index(name = "ix_payments_status", columnList = "status")
})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_user_id", nullable = false)
    private Long tenantUserId;

    @Column(name = "building_id", nullable = false)
    private Long buildingId;

    @Column(name = "bed_id")
    private Long bedId;

    @Column(name = "month_year", nullable = false, length = 7)
    private String monthYear;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.UNPAID;

    private Instant paidAt;

    @Column(length = 50)
    private String receiptNumber;

    @Column(length = 60)
    private String paymentMethod;

    @Column(length = 500)
    private String notes;

    @Column(name = "collected_by_user_id")
    private Long collectedByUserId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantUserId() { return tenantUserId; }
    public void setTenantUserId(Long tenantUserId) { this.tenantUserId = tenantUserId; }
    public Long getBuildingId() { return buildingId; }
    public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }
    public Long getBedId() { return bedId; }
    public void setBedId(Long bedId) { this.bedId = bedId; }
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
    public Long getCollectedByUserId() { return collectedByUserId; }
    public void setCollectedByUserId(Long collectedByUserId) { this.collectedByUserId = collectedByUserId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
