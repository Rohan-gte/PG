package com.example.demo.repository;

import com.example.demo.entity.BuildingImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BuildingImageRepository extends JpaRepository<BuildingImage, Long> {
    List<BuildingImage> findByBuildingIdOrderBySortOrderAsc(Long buildingId);
    void deleteByBuildingId(Long buildingId);
}
