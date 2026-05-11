package com.example.demo.dto.response;

import com.example.demo.entity.Room;
import com.example.demo.enums.SharingType;

import java.math.BigDecimal;
import java.util.List;

public class RoomDto {
    private Long id;
    private Long buildingId;
    private SharingType sharingType;
    private Integer floorNumber;
    private String roomNumber;
    private Integer totalBeds;
    private BigDecimal monthlyRent;
    private BigDecimal depositAmount;
    private long availableBeds;
    private long occupiedBeds;
    private List<BedDto> beds;

    public static RoomDto of(Room r) {
        RoomDto d = new RoomDto();
        d.id = r.getId();
        d.buildingId = r.getBuildingId();
        d.sharingType = r.getSharingType();
        d.floorNumber = r.getFloorNumber();
        d.roomNumber = r.getRoomNumber();
        d.totalBeds = r.getTotalBeds();
        d.monthlyRent = r.getMonthlyRent();
        d.depositAmount = r.getDepositAmount();
        return d;
    }

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
    public long getAvailableBeds() { return availableBeds; }
    public void setAvailableBeds(long availableBeds) { this.availableBeds = availableBeds; }
    public long getOccupiedBeds() { return occupiedBeds; }
    public void setOccupiedBeds(long occupiedBeds) { this.occupiedBeds = occupiedBeds; }
    public List<BedDto> getBeds() { return beds; }
    public void setBeds(List<BedDto> beds) { this.beds = beds; }
}
