package com.example.demo.entity;

import com.example.demo.enums.OwnerStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "owner_profiles")
public class OwnerProfile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 500)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OwnerStatus status = OwnerStatus.PENDING;

    @Column(length = 500)
    private String rejectionReason;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public OwnerStatus getStatus() { return status; }
    public void setStatus(OwnerStatus status) { this.status = status; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
