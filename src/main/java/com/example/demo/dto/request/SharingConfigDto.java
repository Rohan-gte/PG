package com.example.demo.dto.request;

import com.example.demo.enums.SharingType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class SharingConfigDto {
    @NotNull
    private SharingType sharingType;

    @NotNull @Min(1)
    private Integer numRooms;

    @NotNull @Min(1)
    private Integer bedsPerRoom;

    @NotNull @DecimalMin("0.0")
    private BigDecimal monthlyRent;

    @NotNull @DecimalMin("0.0")
    private BigDecimal depositAmount;

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
