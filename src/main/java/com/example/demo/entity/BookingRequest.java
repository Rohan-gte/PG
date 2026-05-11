package com.example.demo.entity;

import com.example.demo.enums.BookingStatus;
import com.example.demo.enums.SharingType;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "booking_requests", indexes = {
        @Index(name = "ix_bookings_tenant", columnList = "tenant_user_id"),
        @Index(name = "ix_bookings_building", columnList = "building_id"),
        @Index(name = "ix_bookings_status", columnList = "status")
})
public class BookingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_user_id", nullable = false)
    private Long tenantUserId;

    @Column(name = "building_id", nullable = false)
    private Long buildingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sharing_type", nullable = false, length = 10)
    private SharingType sharingType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(length = 1000)
    private String notes;

    @Column(length = 500)
    private String decisionNote;

    @Column(name = "allocated_bed_id")
    private Long allocatedBedId;

    @Column(nullable = false, updatable = false)
    private Instant requestedAt = Instant.now();

    private Instant decidedAt;

    @PrePersist
    public void prePersist() {
        if (requestedAt == null) requestedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantUserId() { return tenantUserId; }
    public void setTenantUserId(Long tenantUserId) { this.tenantUserId = tenantUserId; }
    public Long getBuildingId() { return buildingId; }
    public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }
    public SharingType getSharingType() { return sharingType; }
    public void setSharingType(SharingType sharingType) { this.sharingType = sharingType; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getDecisionNote() { return decisionNote; }
    public void setDecisionNote(String decisionNote) { this.decisionNote = decisionNote; }
    public Long getAllocatedBedId() { return allocatedBedId; }
    public void setAllocatedBedId(Long allocatedBedId) { this.allocatedBedId = allocatedBedId; }
    public Instant getRequestedAt() { return requestedAt; }
    public void setRequestedAt(Instant requestedAt) { this.requestedAt = requestedAt; }
    public Instant getDecidedAt() { return decidedAt; }
    public void setDecidedAt(Instant decidedAt) { this.decidedAt = decidedAt; }
}
