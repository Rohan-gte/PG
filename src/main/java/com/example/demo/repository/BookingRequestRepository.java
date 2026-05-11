package com.example.demo.repository;

import com.example.demo.entity.BookingRequest;
import com.example.demo.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRequestRepository extends JpaRepository<BookingRequest, Long> {
    Page<BookingRequest> findByBuildingId(Long buildingId, Pageable pageable);
    Page<BookingRequest> findByBuildingIdAndStatus(Long buildingId, BookingStatus status, Pageable pageable);
    Page<BookingRequest> findByTenantUserId(Long tenantUserId, Pageable pageable);
    List<BookingRequest> findByTenantUserIdAndStatus(Long tenantUserId, BookingStatus status);
    long countByBuildingIdAndStatus(Long buildingId, BookingStatus status);
}
