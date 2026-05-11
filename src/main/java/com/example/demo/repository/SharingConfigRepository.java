package com.example.demo.repository;

import com.example.demo.entity.SharingConfig;
import com.example.demo.enums.SharingType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SharingConfigRepository extends JpaRepository<SharingConfig, Long> {
    List<SharingConfig> findByBuildingId(Long buildingId);
    Optional<SharingConfig> findByBuildingIdAndSharingType(Long buildingId, SharingType sharingType);
    void deleteByBuildingId(Long buildingId);
}
