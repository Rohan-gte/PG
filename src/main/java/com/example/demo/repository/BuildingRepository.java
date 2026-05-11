package com.example.demo.repository;

import com.example.demo.entity.Building;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BuildingRepository extends JpaRepository<Building, Long> {
    List<Building> findByOwnerUserId(Long ownerUserId);
    Page<Building> findByOwnerUserId(Long ownerUserId, Pageable pageable);
    Optional<Building> findByReceptionistUserId(Long receptionistUserId);
    long countByOwnerUserId(Long ownerUserId);

    @Query("select b from Building b where (:q is null or :q = '' or lower(b.name) like lower(concat('%', :q, '%')) or lower(b.city) like lower(concat('%', :q, '%')) or lower(b.area) like lower(concat('%', :q, '%')))")
    Page<Building> search(@Param("q") String q, Pageable pageable);
}
