package com.example.demo.repository;

import com.example.demo.entity.TenantProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TenantProfileRepository extends JpaRepository<TenantProfile, Long> {
    Optional<TenantProfile> findByUserId(Long userId);
    List<TenantProfile> findByCurrentBuildingId(Long buildingId);
    long countByCurrentBuildingId(Long buildingId);
}
