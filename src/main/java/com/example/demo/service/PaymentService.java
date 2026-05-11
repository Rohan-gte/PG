package com.example.demo.service;

import com.example.demo.dto.request.CollectPaymentRequest;
import com.example.demo.dto.response.ApiMessage;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.dto.response.PaymentDto;
import com.example.demo.dto.response.ReceiptDto;
import com.example.demo.entity.Bed;
import com.example.demo.entity.Building;
import com.example.demo.entity.Payment;
import com.example.demo.entity.Room;
import com.example.demo.entity.SharingConfig;
import com.example.demo.entity.TenantProfile;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BedRepository;
import com.example.demo.repository.BuildingRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.RoomRepository;
import com.example.demo.repository.SharingConfigRepository;
import com.example.demo.repository.TenantProfileRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepo;
    private final BedRepository bedRepo;
    private final RoomRepository roomRepo;
    private final BuildingRepository buildingRepo;
    private final UserRepository userRepo;
    private final TenantProfileRepository tenantRepo;
    private final SharingConfigRepository sharingRepo;

    public PaymentService(PaymentRepository paymentRepo,
                          BedRepository bedRepo,
                          RoomRepository roomRepo,
                          BuildingRepository buildingRepo,
                          UserRepository userRepo,
                          TenantProfileRepository tenantRepo,
                          SharingConfigRepository sharingRepo) {
        this.paymentRepo = paymentRepo;
        this.bedRepo = bedRepo;
        this.roomRepo = roomRepo;
        this.buildingRepo = buildingRepo;
        this.userRepo = userRepo;
        this.tenantRepo = tenantRepo;
        this.sharingRepo = sharingRepo;
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentDto> listForReceptionist(Long receptionistUserId,
                                                       PaymentStatus status,
                                                       Pageable pageable) {
        Building b = buildingRepo.findByReceptionistUserId(receptionistUserId)
                .orElseThrow(() -> new ForbiddenException("No building assigned"));
        markOverdue(b.getId());
        Page<Payment> page = status == null
                ? paymentRepo.findByBuildingId(b.getId(), pageable)
                : paymentRepo.findByBuildingIdAndStatus(b.getId(), status, pageable);
        return PageResponse.of(page, this::hydrate);
    }

    @Transactional(readOnly = true)
    public List<PaymentDto> listForTenant(Long tenantUserId) {
        markOverdueForTenant(tenantUserId);
        return paymentRepo.findByTenantUserIdOrderByMonthYearDesc(tenantUserId).stream()
                .map(this::hydrate).toList();
    }

    @Transactional
    public PaymentDto collect(Long receptionistUserId, CollectPaymentRequest req) {
        Building b = buildingRepo.findByReceptionistUserId(receptionistUserId)
                .orElseThrow(() -> new ForbiddenException("No building assigned"));
        Payment p = paymentRepo.findById(req.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        if (!p.getBuildingId().equals(b.getId())) {
            throw new ForbiddenException("Payment not in your building");
        }
        if (p.getStatus() == PaymentStatus.PAID) {
            throw new BadRequestException("Already paid");
        }
        if (req.getAmount() != null && req.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            p.setAmount(req.getAmount());
        }
        p.setStatus(PaymentStatus.PAID);
        p.setPaidAt(Instant.now());
        p.setPaymentMethod(req.getPaymentMethod() == null ? "CASH" : req.getPaymentMethod());
        p.setNotes(req.getNotes());
        p.setCollectedByUserId(receptionistUserId);
        if (p.getReceiptNumber() == null) {
            p.setReceiptNumber("RCPT-" + p.getMonthYear().replace("-", "") + "-" + p.getId());
        }
        paymentRepo.save(p);
        return hydrate(p);
    }

    @Transactional
    public PaymentDto tenantPay(Long tenantUserId, Long paymentId) {
        Payment p = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        if (!p.getTenantUserId().equals(tenantUserId)) {
            throw new ForbiddenException("Not your payment");
        }
        if (p.getStatus() == PaymentStatus.PAID) {
            throw new BadRequestException("Already paid");
        }
        p.setStatus(PaymentStatus.PAID);
        p.setPaidAt(Instant.now());
        p.setPaymentMethod("ONLINE");
        p.setCollectedByUserId(tenantUserId);
        if (p.getReceiptNumber() == null) {
            p.setReceiptNumber("RCPT-" + p.getMonthYear().replace("-", "") + "-" + p.getId());
        }
        paymentRepo.save(p);
        return hydrate(p);
    }

    @Transactional
    public ApiMessage generateMonth(Long receptionistUserId, String monthYear) {
        Building b = buildingRepo.findByReceptionistUserId(receptionistUserId)
                .orElseThrow(() -> new ForbiddenException("No building assigned"));
        if (monthYear == null || !monthYear.matches("\\d{4}-\\d{2}")) {
            monthYear = java.time.YearMonth.now(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        int created = 0;
        for (TenantProfile tp : tenantRepo.findByCurrentBuildingId(b.getId())) {
            if (tp.getCurrentBedId() == null) continue;
            if (paymentRepo.findByTenantUserIdAndMonthYear(tp.getUserId(), monthYear).isPresent()) {
                continue;
            }
            Bed bed = bedRepo.findById(tp.getCurrentBedId()).orElse(null);
            if (bed == null) continue;
            Room room = roomRepo.findById(bed.getRoomId()).orElse(null);
            if (room == null) continue;
            BigDecimal amount = room.getMonthlyRent();
            if (amount == null) {
                SharingConfig sc = sharingRepo.findByBuildingIdAndSharingType(b.getId(), room.getSharingType())
                        .orElse(null);
                if (sc != null) amount = sc.getMonthlyRent();
            }
            if (amount == null) continue;
            Payment p = new Payment();
            p.setTenantUserId(tp.getUserId());
            p.setBuildingId(b.getId());
            p.setBedId(bed.getId());
            p.setMonthYear(monthYear);
            p.setAmount(amount);
            p.setStatus(PaymentStatus.UNPAID);
            paymentRepo.save(p);
            created++;
        }
        return new ApiMessage("Generated " + created + " payment record(s) for " + monthYear);
    }

    @Transactional
    public ReceiptDto getReceipt(String receiptNumber, Long requesterUserId, String requesterRole) {
        Payment p = paymentRepo.findByReceiptNumber(receiptNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found"));
        if (p.getStatus() != PaymentStatus.PAID) {
            throw new BadRequestException("Receipt is only available for paid payments");
        }
        if ("TENANT".equals(requesterRole) && !p.getTenantUserId().equals(requesterUserId)) {
            throw new ForbiddenException("Not your receipt");
        }
        if ("RECEPTIONIST".equals(requesterRole)) {
            Building b = buildingRepo.findByReceptionistUserId(requesterUserId).orElse(null);
            if (b == null || !b.getId().equals(p.getBuildingId())) {
                throw new ForbiddenException("Not your building");
            }
        }
        if ("OWNER".equals(requesterRole)) {
            Building b = buildingRepo.findById(p.getBuildingId()).orElse(null);
            if (b == null || !b.getOwnerUserId().equals(requesterUserId)) {
                throw new ForbiddenException("Not your building");
            }
        }
        ReceiptDto r = new ReceiptDto();
        r.setReceiptNumber(p.getReceiptNumber());
        r.setMonthYear(p.getMonthYear());
        r.setAmount(p.getAmount());
        r.setPaymentMethod(p.getPaymentMethod());
        r.setPaidAt(p.getPaidAt());
        userRepo.findById(p.getTenantUserId()).ifPresent(u -> {
            r.setTenantName(u.getFullName());
            r.setTenantEmail(u.getEmail());
            r.setTenantPhone(u.getPhone());
        });
        buildingRepo.findById(p.getBuildingId()).ifPresent(b -> {
            r.setBuildingName(b.getName());
            r.setBuildingAddress(b.getAddress());
        });
        if (p.getBedId() != null) {
            Bed bed = bedRepo.findById(p.getBedId()).orElse(null);
            if (bed != null) {
                r.setBedNumber(bed.getBedNumber());
                roomRepo.findById(bed.getRoomId()).ifPresent(rm -> r.setRoomNumber(rm.getRoomNumber()));
            }
        }
        if (p.getCollectedByUserId() != null) {
            userRepo.findById(p.getCollectedByUserId()).ifPresent(u -> r.setCollectedByName(u.getFullName()));
        }
        return r;
    }

    private void markOverdue(Long buildingId) {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        for (Payment p : paymentRepo.findAllUnpaid()) {
            if (!buildingId.equals(p.getBuildingId())) continue;
            if (isOverdue(p.getMonthYear(), today)) {
                p.setStatus(PaymentStatus.OVERDUE);
                paymentRepo.save(p);
            }
        }
    }

    private void markOverdueForTenant(Long tenantUserId) {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        for (Payment p : paymentRepo.findByTenantUserIdOrderByMonthYearDesc(tenantUserId)) {
            if (p.getStatus() == PaymentStatus.UNPAID && isOverdue(p.getMonthYear(), today)) {
                p.setStatus(PaymentStatus.OVERDUE);
                paymentRepo.save(p);
            }
        }
    }

    private boolean isOverdue(String monthYear, LocalDate today) {
        try {
            String[] parts = monthYear.split("-");
            int y = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            LocalDate due = LocalDate.of(y, m, 1).plusMonths(1).plusDays(5);
            return today.isAfter(due);
        } catch (Exception ex) {
            return false;
        }
    }

    public PaymentDto hydrate(Payment p) {
        PaymentDto d = PaymentDto.of(p);
        userRepo.findById(p.getTenantUserId()).ifPresent(u -> {
            d.setTenantName(u.getFullName());
            d.setTenantEmail(u.getEmail());
        });
        buildingRepo.findById(p.getBuildingId()).ifPresent(b -> d.setBuildingName(b.getName()));
        if (p.getBedId() != null) {
            bedRepo.findById(p.getBedId()).ifPresent(bed -> {
                d.setBedNumber(bed.getBedNumber());
                roomRepo.findById(bed.getRoomId()).ifPresent(r -> d.setRoomNumber(r.getRoomNumber()));
            });
        }
        return d;
    }
}
