package com.example.demo.dto.response;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardDto {
    private long totalBuildings;
    private long totalTenants;
    private long totalOwners;
    private long pendingOwners;
    private long totalReceptionists;
    private long totalBeds;
    private long occupiedBeds;
    private long availableBeds;
    private long pendingRequests;
    private long pendingPayments;
    private long overduePayments;
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private BigDecimal monthRevenue = BigDecimal.ZERO;
    private Map<String, Long> occupancyBySharing = new LinkedHashMap<>();
    private Map<String, Long> bedsBySharing = new LinkedHashMap<>();
    private List<BuildingDto> buildings;
    private Long allocatedBedId;
    private String allocatedBedNumber;
    private String allocatedRoomNumber;
    private Long allocatedBuildingId;
    private String allocatedBuildingName;

    public long getTotalBuildings() { return totalBuildings; }
    public void setTotalBuildings(long totalBuildings) { this.totalBuildings = totalBuildings; }
    public long getTotalTenants() { return totalTenants; }
    public void setTotalTenants(long totalTenants) { this.totalTenants = totalTenants; }
    public long getTotalOwners() { return totalOwners; }
    public void setTotalOwners(long totalOwners) { this.totalOwners = totalOwners; }
    public long getPendingOwners() { return pendingOwners; }
    public void setPendingOwners(long pendingOwners) { this.pendingOwners = pendingOwners; }
    public long getTotalReceptionists() { return totalReceptionists; }
    public void setTotalReceptionists(long totalReceptionists) { this.totalReceptionists = totalReceptionists; }
    public long getTotalBeds() { return totalBeds; }
    public void setTotalBeds(long totalBeds) { this.totalBeds = totalBeds; }
    public long getOccupiedBeds() { return occupiedBeds; }
    public void setOccupiedBeds(long occupiedBeds) { this.occupiedBeds = occupiedBeds; }
    public long getAvailableBeds() { return availableBeds; }
    public void setAvailableBeds(long availableBeds) { this.availableBeds = availableBeds; }
    public long getPendingRequests() { return pendingRequests; }
    public void setPendingRequests(long pendingRequests) { this.pendingRequests = pendingRequests; }
    public long getPendingPayments() { return pendingPayments; }
    public void setPendingPayments(long pendingPayments) { this.pendingPayments = pendingPayments; }
    public long getOverduePayments() { return overduePayments; }
    public void setOverduePayments(long overduePayments) { this.overduePayments = overduePayments; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    public BigDecimal getMonthRevenue() { return monthRevenue; }
    public void setMonthRevenue(BigDecimal monthRevenue) { this.monthRevenue = monthRevenue; }
    public Map<String, Long> getOccupancyBySharing() { return occupancyBySharing; }
    public void setOccupancyBySharing(Map<String, Long> occupancyBySharing) { this.occupancyBySharing = occupancyBySharing; }
    public Map<String, Long> getBedsBySharing() { return bedsBySharing; }
    public void setBedsBySharing(Map<String, Long> bedsBySharing) { this.bedsBySharing = bedsBySharing; }
    public List<BuildingDto> getBuildings() { return buildings; }
    public void setBuildings(List<BuildingDto> buildings) { this.buildings = buildings; }
    public Long getAllocatedBedId() { return allocatedBedId; }
    public void setAllocatedBedId(Long allocatedBedId) { this.allocatedBedId = allocatedBedId; }
    public String getAllocatedBedNumber() { return allocatedBedNumber; }
    public void setAllocatedBedNumber(String allocatedBedNumber) { this.allocatedBedNumber = allocatedBedNumber; }
    public String getAllocatedRoomNumber() { return allocatedRoomNumber; }
    public void setAllocatedRoomNumber(String allocatedRoomNumber) { this.allocatedRoomNumber = allocatedRoomNumber; }
    public Long getAllocatedBuildingId() { return allocatedBuildingId; }
    public void setAllocatedBuildingId(Long allocatedBuildingId) { this.allocatedBuildingId = allocatedBuildingId; }
    public String getAllocatedBuildingName() { return allocatedBuildingName; }
    public void setAllocatedBuildingName(String allocatedBuildingName) { this.allocatedBuildingName = allocatedBuildingName; }
}
