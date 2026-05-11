package com.example.demo.entity;

import com.example.demo.enums.SharingType;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "rooms", uniqueConstraints = {
        @UniqueConstraint(name = "uk_rooms_building_number", columnNames = {"building_id", "room_number"})
}, indexes = {
        @Index(name = "ix_rooms_building", columnList = "building_id"),
        @Index(name = "ix_rooms_sharing", columnList = "sharing_type")
})
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "building_id", nullable = false)
    private Long buildingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sharing_type", nullable = false, length = 10)
    private SharingType sharingType;

    @Column(nullable = false)
    private Integer floorNumber;

    @Column(name = "room_number", nullable = false, length = 30)
    private String roomNumber;

    @Column(nullable = false)
    private Integer totalBeds;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyRent;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal depositAmount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBuildingId() { return buildingId; }
    public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }
    public SharingType getSharingType() { return sharingType; }
    public void setSharingType(SharingType sharingType) { this.sharingType = sharingType; }
    public Integer getFloorNumber() { return floorNumber; }
    public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public Integer getTotalBeds() { return totalBeds; }
    public void setTotalBeds(Integer totalBeds) { this.totalBeds = totalBeds; }
    public BigDecimal getMonthlyRent() { return monthlyRent; }
    public void setMonthlyRent(BigDecimal monthlyRent) { this.monthlyRent = monthlyRent; }
    public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }
}
