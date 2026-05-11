package com.example.demo.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public class ReceiptDto {
    private String receiptNumber;
    private String tenantName;
    private String tenantEmail;
    private String tenantPhone;
    private String buildingName;
    private String buildingAddress;
    private String roomNumber;
    private String bedNumber;
    private String monthYear;
    private BigDecimal amount;
    private String paymentMethod;
    private Instant paidAt;
    private String collectedByName;

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }
    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }
    public String getTenantEmail() { return tenantEmail; }
    public void setTenantEmail(String tenantEmail) { this.tenantEmail = tenantEmail; }
    public String getTenantPhone() { return tenantPhone; }
    public void setTenantPhone(String tenantPhone) { this.tenantPhone = tenantPhone; }
    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }
    public String getBuildingAddress() { return buildingAddress; }
    public void setBuildingAddress(String buildingAddress) { this.buildingAddress = buildingAddress; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getBedNumber() { return bedNumber; }
    public void setBedNumber(String bedNumber) { this.bedNumber = bedNumber; }
    public String getMonthYear() { return monthYear; }
    public void setMonthYear(String monthYear) { this.monthYear = monthYear; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
    public String getCollectedByName() { return collectedByName; }
    public void setCollectedByName(String collectedByName) { this.collectedByName = collectedByName; }
}
