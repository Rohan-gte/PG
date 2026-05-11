package com.example.demo.dto.response;

import java.util.LinkedHashMap;
import java.util.Map;

public class AvailabilityDto {
    private Long buildingId;
    private long totalBeds;
    private long occupiedBeds;
    private long availableBeds;
    private Map<String, SharingAvailability> perSharing = new LinkedHashMap<>();

    public static class SharingAvailability {
        public long total;
        public long occupied;
        public long available;

        public SharingAvailability() {}
        public SharingAvailability(long total, long occupied, long available) {
            this.total = total;
            this.occupied = occupied;
            this.available = available;
        }

        public long getTotal() { return total; }
        public long getOccupied() { return occupied; }
        public long getAvailable() { return available; }
    }

    public Long getBuildingId() { return buildingId; }
    public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }
    public long getTotalBeds() { return totalBeds; }
    public void setTotalBeds(long totalBeds) { this.totalBeds = totalBeds; }
    public long getOccupiedBeds() { return occupiedBeds; }
    public void setOccupiedBeds(long occupiedBeds) { this.occupiedBeds = occupiedBeds; }
    public long getAvailableBeds() { return availableBeds; }
    public void setAvailableBeds(long availableBeds) { this.availableBeds = availableBeds; }
    public Map<String, SharingAvailability> getPerSharing() { return perSharing; }
    public void setPerSharing(Map<String, SharingAvailability> perSharing) { this.perSharing = perSharing; }
}
