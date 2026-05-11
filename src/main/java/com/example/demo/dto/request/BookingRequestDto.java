package com.example.demo.dto.request;

import com.example.demo.enums.SharingType;
import jakarta.validation.constraints.*;

public class BookingRequestDto {
    @NotNull
    private Long buildingId;

    @NotNull
    private SharingType sharingType;

    @Size(max = 1000)
    private String notes;

    public Long getBuildingId() { return buildingId; }
    public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }
    public SharingType getSharingType() { return sharingType; }
    public void setSharingType(SharingType sharingType) { this.sharingType = sharingType; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
