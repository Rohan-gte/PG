package com.example.demo.repository;

import com.example.demo.entity.OwnerProfile;
import com.example.demo.enums.OwnerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OwnerProfileRepository extends JpaRepository<OwnerProfile, Long> {
    Optional<OwnerProfile> findByUserId(Long userId);
    Page<OwnerProfile> findByStatus(OwnerStatus status, Pageable pageable);
    List<OwnerProfile> findByStatus(OwnerStatus status);
    long countByStatus(OwnerStatus status);
}
