package com.example.demo.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class UpdateRoomRequest {
    @NotNull @DecimalMin("0.0")
    private BigDecimal monthlyRent;

    @NotNull @DecimalMin("0.0")
    private BigDecimal depositAmount;

    private Integer floorNumber;

    public BigDecimal getMonthlyRent() { return monthlyRent; }
    public void setMonthlyRent(BigDecimal monthlyRent) { this.monthlyRent = monthlyRent; }
    public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }
    public Integer getFloorNumber() { return floorNumber; }
    public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }
}
