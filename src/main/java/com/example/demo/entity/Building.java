package com.example.demo.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "buildings", uniqueConstraints = {
        @UniqueConstraint(name = "uk_buildings_receptionist", columnNames = "receptionist_user_id")
}, indexes = {
        @Index(name = "ix_buildings_owner", columnList = "owner_user_id"),
        @Index(name = "ix_buildings_city", columnList = "city"),
        @Index(name = "ix_buildings_area", columnList = "area")
})
public class Building {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String address;

    @Column(length = 120)
    private String area;

    @Column(length = 120)
    private String city;

    @Column(length = 2000)
    private String description;

    @Column(length = 1000)
    private String amenitiesCsv;

    @Column(nullable = false)
    private Integer totalFloors = 1;

    @Column(length = 25)
    private String contactPhone;

    @Column(length = 180)
    private String contactEmail;

    @Column(name = "receptionist_user_id")
    private Long receptionistUserId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }
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
    public String getAmenitiesCsv() { return amenitiesCsv; }
    public void setAmenitiesCsv(String amenitiesCsv) { this.amenitiesCsv = amenitiesCsv; }
    public Integer getTotalFloors() { return totalFloors; }
    public void setTotalFloors(Integer totalFloors) { this.totalFloors = totalFloors; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public Long getReceptionistUserId() { return receptionistUserId; }
    public void setReceptionistUserId(Long receptionistUserId) { this.receptionistUserId = receptionistUserId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
