package com.example.demo.dto.response;

import com.example.demo.entity.Bed;
import com.example.demo.enums.BedStatus;

public class BedDto {
    private Long id;
    private Long roomId;
    private Long buildingId;
    private String bedNumber;
    private BedStatus status;
    private Long tenantUserId;
    private String tenantName;

    public static BedDto of(Bed b) {
        BedDto d = new BedDto();
        d.id = b.getId();
        d.roomId = b.getRoomId();
        d.buildingId = b.getBuildingId();
        d.bedNumber = b.getBedNumber();
        d.status = b.getStatus();
        d.tenantUserId = b.getTenantUserId();
        return d;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public Long getBuildingId() { return buildingId; }
    public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }
    public String getBedNumber() { return bedNumber; }
    public void setBedNumber(String bedNumber) { this.bedNumber = bedNumber; }
    public BedStatus getStatus() { return status; }
    public void setStatus(BedStatus status) { this.status = status; }
    public Long getTenantUserId() { return tenantUserId; }
    public void setTenantUserId(Long tenantUserId) { this.tenantUserId = tenantUserId; }
    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }
}
