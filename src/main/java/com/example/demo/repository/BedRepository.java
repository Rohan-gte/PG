package com.example.demo.repository;

import com.example.demo.entity.Bed;
import com.example.demo.enums.BedStatus;
import com.example.demo.enums.SharingType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BedRepository extends JpaRepository<Bed, Long> {
    List<Bed> findByRoomId(Long roomId);
    List<Bed> findByBuildingId(Long buildingId);
    Optional<Bed> findByTenantUserId(Long tenantUserId);

    long countByBuildingIdAndStatus(Long buildingId, BedStatus status);
    long countByBuildingId(Long buildingId);

    @Query("select count(b) from Bed b join Room r on b.roomId = r.id where r.buildingId = :buildingId and r.sharingType = :sharingType and b.status = :status")
    long countByBuildingAndSharingAndStatus(@Param("buildingId") Long buildingId,
                                            @Param("sharingType") SharingType sharingType,
                                            @Param("status") BedStatus status);

    @Query("select count(b) from Bed b join Room r on b.roomId = r.id where r.buildingId = :buildingId and r.sharingType = :sharingType")
    long countByBuildingAndSharing(@Param("buildingId") Long buildingId,
                                   @Param("sharingType") SharingType sharingType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Bed b where b.id = :id")
    Optional<Bed> findByIdForUpdate(@Param("id") Long id);

    @Query("select b from Bed b join Room r on b.roomId = r.id where r.buildingId = :buildingId and r.sharingType = :sharingType and b.status = 'AVAILABLE' order by b.id asc")
    List<Bed> findAvailableByBuildingAndSharing(@Param("buildingId") Long buildingId,
                                                @Param("sharingType") SharingType sharingType);

    void deleteByRoomId(Long roomId);
    void deleteByBuildingId(Long buildingId);
}
