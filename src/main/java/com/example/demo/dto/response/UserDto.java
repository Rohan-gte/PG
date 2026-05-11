package com.example.demo.dto.response;

import com.example.demo.entity.User;
import com.example.demo.enums.OwnerStatus;
import com.example.demo.enums.Role;

import java.time.Instant;

public class UserDto {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private boolean enabled;
    private Instant createdAt;
    private OwnerStatus ownerStatus;
    private Long buildingId;

    public static UserDto of(User u) {
        UserDto d = new UserDto();
        d.id = u.getId();
        d.fullName = u.getFullName();
        d.email = u.getEmail();
        d.phone = u.getPhone();
        d.role = u.getRole();
        d.enabled = u.isEnabled();
        d.createdAt = u.getCreatedAt();
        return d;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public OwnerStatus getOwnerStatus() { return ownerStatus; }
    public void setOwnerStatus(OwnerStatus ownerStatus) { this.ownerStatus = ownerStatus; }
    public Long getBuildingId() { return buildingId; }
    public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }
}
