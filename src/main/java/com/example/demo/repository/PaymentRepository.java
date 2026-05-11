package com.example.demo.repository;

import com.example.demo.entity.Payment;
import com.example.demo.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findByBuildingId(Long buildingId, Pageable pageable);
    Page<Payment> findByBuildingIdAndStatus(Long buildingId, PaymentStatus status, Pageable pageable);
    Page<Payment> findByTenantUserId(Long tenantUserId, Pageable pageable);
    List<Payment> findByTenantUserIdOrderByMonthYearDesc(Long tenantUserId);
    Optional<Payment> findByReceiptNumber(String receiptNumber);
    Optional<Payment> findByTenantUserIdAndMonthYear(Long tenantUserId, String monthYear);

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.status = 'PAID'")
    BigDecimal sumPaidGlobal();

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.buildingId = :buildingId and p.status = 'PAID'")
    BigDecimal sumPaidByBuilding(@Param("buildingId") Long buildingId);

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.buildingId in :buildingIds and p.status = 'PAID'")
    BigDecimal sumPaidByBuildings(@Param("buildingIds") List<Long> buildingIds);

    long countByStatus(PaymentStatus status);
    long countByBuildingIdAndStatus(Long buildingId, PaymentStatus status);

    @Query("select p from Payment p where p.status = 'UNPAID'")
    List<Payment> findAllUnpaid();
}
