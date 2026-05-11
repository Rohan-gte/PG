package com.example.demo.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.ArrayList;
import java.util.List;

public class CreateBuildingRequest {
    @NotBlank @Size(max = 200)
    private String name;

    @NotBlank @Size(max = 500)
    private String address;

    @NotBlank @Size(max = 120)
    private String area;

    @NotBlank @Size(max = 120)
    private String city;

    @Size(max = 2000)
    private String description;

    private List<String> amenities = new ArrayList<>();

    @NotNull @Min(1) @Max(50)
    private Integer totalFloors;

    @Pattern(regexp = "^[+0-9 \\-]{7,20}$", message = "Invalid phone")
    private String contactPhone;

    @Email
    private String contactEmail;

    private List<String> imagePaths = new ArrayList<>();

    @Valid @NotNull
    private ReceptionistRequest receptionist;

    @Valid
    @Size(min = 1, message = "At least one sharing config is required")
    private List<SharingConfigDto> sharingConfigs = new ArrayList<>();

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
    public List<String> getImagePaths() { return imagePaths; }
    public void setImagePaths(List<String> imagePaths) { this.imagePaths = imagePaths; }
    public ReceptionistRequest getReceptionist() { return receptionist; }
    public void setReceptionist(ReceptionistRequest receptionist) { this.receptionist = receptionist; }
    public List<SharingConfigDto> getSharingConfigs() { return sharingConfigs; }
    public void setSharingConfigs(List<SharingConfigDto> sharingConfigs) { this.sharingConfigs = sharingConfigs; }
}
