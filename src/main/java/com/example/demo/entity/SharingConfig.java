package com.example.demo.entity;

import com.example.demo.enums.SharingType;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "sharing_configs", uniqueConstraints = {
        @UniqueConstraint(name = "uk_sharing_building_type", columnNames = {"building_id", "sharing_type"})
})
public class SharingConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "building_id", nullable = false)
    private Long buildingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sharing_type", nullable = false, length = 10)
    private SharingType sharingType;

    @Column(nullable = false)
    private Integer numRooms;

    @Column(nullable = false)
    private Integer bedsPerRoom;

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
    public Integer getNumRooms() { return numRooms; }
    public void setNumRooms(Integer numRooms) { this.numRooms = numRooms; }
    public Integer getBedsPerRoom() { return bedsPerRoom; }
    public void setBedsPerRoom(Integer bedsPerRoom) { this.bedsPerRoom = bedsPerRoom; }
    public BigDecimal getMonthlyRent() { return monthlyRent; }
    public void setMonthlyRent(BigDecimal monthlyRent) { this.monthlyRent = monthlyRent; }
    public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }
}
