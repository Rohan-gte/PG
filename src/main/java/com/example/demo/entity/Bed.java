package com.example.demo.entity;

import com.example.demo.enums.BedStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "beds", uniqueConstraints = {
        @UniqueConstraint(name = "uk_beds_room_number", columnNames = {"room_id", "bed_number"})
}, indexes = {
        @Index(name = "ix_beds_room", columnList = "room_id"),
        @Index(name = "ix_beds_status", columnList = "status"),
        @Index(name = "ix_beds_tenant", columnList = "tenant_user_id")
})
public class Bed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "building_id", nullable = false)
    private Long buildingId;

    @Column(name = "bed_number", nullable = false, length = 20)
    private String bedNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BedStatus status = BedStatus.AVAILABLE;

    @Column(name = "tenant_user_id")
    private Long tenantUserId;

    @Version
    private Long version;

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
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
