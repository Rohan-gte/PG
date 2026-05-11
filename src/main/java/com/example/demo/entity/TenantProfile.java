package com.example.demo.entity;

import com.example.demo.enums.Gender;
import jakarta.persistence.*;

@Entity
@Table(name = "tenant_profiles")
public class TenantProfile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column(length = 500)
    private String idProofPath;

    @Column(name = "current_bed_id")
    private Long currentBedId;

    @Column(name = "current_building_id")
    private Long currentBuildingId;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    public String getIdProofPath() { return idProofPath; }
    public void setIdProofPath(String idProofPath) { this.idProofPath = idProofPath; }
    public Long getCurrentBedId() { return currentBedId; }
    public void setCurrentBedId(Long currentBedId) { this.currentBedId = currentBedId; }
    public Long getCurrentBuildingId() { return currentBuildingId; }
    public void setCurrentBuildingId(Long currentBuildingId) { this.currentBuildingId = currentBuildingId; }
}
