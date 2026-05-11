package com.example.demo.dto.response;

import com.example.demo.entity.Building;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BuildingDto {
    private Long id;
    private Long ownerUserId;
    private String ownerName;
    private String name;
    private String address;
    private String area;
    private String city;
    private String description;
    private List<String> amenities = new ArrayList<>();
    private Integer totalFloors;
    private String contactPhone;
    private String contactEmail;
    private Long receptionistUserId;
    private String receptionistName;
    private String receptionistEmail;
    private List<String> imagePaths = new ArrayList<>();
    private List<SharingConfigResponseDto> sharingConfigs = new ArrayList<>();
    private AvailabilityDto availability;
    private Instant createdAt;

    public static BuildingDto of(Building b) {
        BuildingDto d = new BuildingDto();
        d.id = b.getId();
        d.ownerUserId = b.getOwnerUserId();
        d.name = b.getName();
        d.address = b.getAddress();
        d.area = b.getArea();
        d.city = b.getCity();
        d.description = b.getDescription();
        if (b.getAmenitiesCsv() != null && !b.getAmenitiesCsv().isEmpty()) {
            d.amenities = Arrays.stream(b.getAmenitiesCsv().split(","))
                    .map(String::trim).filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }
        d.totalFloors = b.getTotalFloors();
        d.contactPhone = b.getContactPhone();
        d.contactEmail = b.getContactEmail();
        d.receptionistUserId = b.getReceptionistUserId();
        d.createdAt = b.getCreatedAt();
        return d;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getAmenities() { return amenities; }
    public void setAmenities(List<String> amenities) { this.amenities = amenities; }
    public Integer getTotalFloors() { return totalFloors; }
    public void setTotalFloors(Integer totalFloors) { this.totalFloors = totalFloors; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public Long getReceptionistUserId() { return receptionistUserId; }
    public void setReceptionistUserId(Long receptionistUserId) { this.receptionistUserId = receptionistUserId; }
    public String getReceptionistName() { return receptionistName; }
    public void setReceptionistName(String receptionistName) { this.receptionistName = receptionistName; }
    public String getReceptionistEmail() { return receptionistEmail; }
    public void setReceptionistEmail(String receptionistEmail) { this.receptionistEmail = receptionistEmail; }
    public List<String> getImagePaths() { return imagePaths; }
    public void setImagePaths(List<String> imagePaths) { this.imagePaths = imagePaths; }
    public List<SharingConfigResponseDto> getSharingConfigs() { return sharingConfigs; }
    public void setSharingConfigs(List<SharingConfigResponseDto> sharingConfigs) { this.sharingConfigs = sharingConfigs; }
    public AvailabilityDto getAvailability() { return availability; }
    public void setAvailability(AvailabilityDto availability) { this.availability = availability; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
