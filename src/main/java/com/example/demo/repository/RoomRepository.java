package com.example.demo.repository;

import com.example.demo.entity.Room;
import com.example.demo.enums.SharingType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByBuildingId(Long buildingId);
    List<Room> findByBuildingIdAndSharingType(Long buildingId, SharingType sharingType);
    long countByBuildingId(Long buildingId);
    long countByBuildingIdAndSharingType(Long buildingId, SharingType sharingType);
    void deleteByBuildingId(Long buildingId);
}
