package com.example.demo.dto.response;

import com.example.demo.entity.SharingConfig;
import com.example.demo.enums.SharingType;

import java.math.BigDecimal;

public class SharingConfigResponseDto {
    private Long id;
    private SharingType sharingType;
    private Integer numRooms;
    private Integer bedsPerRoom;
    private BigDecimal monthlyRent;
    private BigDecimal depositAmount;
    private Long availableBeds;
    private Long totalBeds;

    public static SharingConfigResponseDto of(SharingConfig s) {
        SharingConfigResponseDto d = new SharingConfigResponseDto();
        d.id = s.getId();
        d.sharingType = s.getSharingType();
        d.numRooms = s.getNumRooms();
        d.bedsPerRoom = s.getBedsPerRoom();
        d.monthlyRent = s.getMonthlyRent();
        d.depositAmount = s.getDepositAmount();
        return d;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public Long getAvailableBeds() { return availableBeds; }
    public void setAvailableBeds(Long availableBeds) { this.availableBeds = availableBeds; }
    public Long getTotalBeds() { return totalBeds; }
    public void setTotalBeds(Long totalBeds) { this.totalBeds = totalBeds; }
}
