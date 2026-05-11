package com.example.demo.dto.response;

import com.example.demo.entity.BookingRequest;
import com.example.demo.enums.BookingStatus;
import com.example.demo.enums.SharingType;

import java.time.Instant;

public class BookingRequestResponseDto {
    private Long id;
    private Long tenantUserId;
    private String tenantName;
    private String tenantEmail;
    private String tenantPhone;
    private Long buildingId;
    private String buildingName;
    private SharingType sharingType;
    private BookingStatus status;
    private String notes;
    private String decisionNote;
    private Long allocatedBedId;
    private String allocatedBedNumber;
    private String allocatedRoomNumber;
    private Instant requestedAt;
    private Instant decidedAt;

    public static BookingRequestResponseDto of(BookingRequest b) {
        BookingRequestResponseDto d = new BookingRequestResponseDto();
        d.id = b.getId();
        d.tenantUserId = b.getTenantUserId();
        d.buildingId = b.getBuildingId();
        d.sharingType = b.getSharingType();
        d.status = b.getStatus();
        d.notes = b.getNotes();
        d.decisionNote = b.getDecisionNote();
        d.allocatedBedId = b.getAllocatedBedId();
        d.requestedAt = b.getRequestedAt();
        d.decidedAt = b.getDecidedAt();
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
    public String getTenantPhone() { return tenantPhone; }
    public void setTenantPhone(String tenantPhone) { this.tenantPhone = tenantPhone; }
    public Long getBuildingId() { return buildingId; }
    public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }
    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }
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
    public String getAllocatedBedNumber() { return allocatedBedNumber; }
    public void setAllocatedBedNumber(String allocatedBedNumber) { this.allocatedBedNumber = allocatedBedNumber; }
    public String getAllocatedRoomNumber() { return allocatedRoomNumber; }
    public void setAllocatedRoomNumber(String allocatedRoomNumber) { this.allocatedRoomNumber = allocatedRoomNumber; }
    public Instant getRequestedAt() { return requestedAt; }
    public void setRequestedAt(Instant requestedAt) { this.requestedAt = requestedAt; }
    public Instant getDecidedAt() { return decidedAt; }
    public void setDecidedAt(Instant decidedAt) { this.decidedAt = decidedAt; }
}
