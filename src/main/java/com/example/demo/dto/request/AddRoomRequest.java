package com.example.demo.dto.request;

import com.example.demo.enums.SharingType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class AddRoomRequest {
    @NotNull
    private SharingType sharingType;

    @NotNull @Min(1)
    private Integer floorNumber;

    @NotBlank
    private String roomNumber;

    @NotNull @Min(1)
    private Integer totalBeds;

    @NotNull @DecimalMin("0.0")
    private BigDecimal monthlyRent;

    @NotNull @DecimalMin("0.0")
    private BigDecimal depositAmount;

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
