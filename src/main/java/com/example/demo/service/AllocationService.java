package com.example.demo.service;

import com.example.demo.dto.response.ApiMessage;
import com.example.demo.dto.response.BookingRequestResponseDto;
import com.example.demo.entity.Bed;
import com.example.demo.entity.BookingRequest;
import com.example.demo.entity.Building;
import com.example.demo.entity.Payment;
import com.example.demo.entity.Room;
import com.example.demo.entity.SharingConfig;
import com.example.demo.entity.TenantProfile;
import com.example.demo.enums.BedStatus;
import com.example.demo.enums.BookingStatus;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BedRepository;
import com.example.demo.repository.BookingRequestRepository;
import com.example.demo.repository.BuildingRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.RoomRepository;
import com.example.demo.repository.SharingConfigRepository;
import com.example.demo.repository.TenantProfileRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class AllocationService {

    private final BookingRequestRepository bookingRepo;
    private final BedRepository bedRepo;
    private final RoomRepository roomRepo;
    private final BuildingRepository buildingRepo;
    private final TenantProfileRepository tenantRepo;
    private final SharingConfigRepository sharingRepo;
    private final UserRepository userRepo;
    private final PaymentRepository paymentRepo;

    public AllocationService(BookingRequestRepository bookingRepo,
                             BedRepository bedRepo,
                             RoomRepository roomRepo,
                             BuildingRepository buildingRepo,
                             TenantProfileRepository tenantRepo,
                             SharingConfigRepository sharingRepo,
                             UserRepository userRepo,
                             PaymentRepository paymentRepo) {
        this.bookingRepo = bookingRepo;
        this.bedRepo = bedRepo;
        this.roomRepo = roomRepo;
        this.buildingRepo = buildingRepo;
        this.tenantRepo = tenantRepo;
        this.sharingRepo = sharingRepo;
        this.userRepo = userRepo;
        this.paymentRepo = paymentRepo;
    }

    @Transactional
    public BookingRequestResponseDto allocate(Long receptionistUserId, Long bookingRequestId, Long bedId) {
        Building building = buildingRepo.findByReceptionistUserId(receptionistUserId)
                .orElseThrow(() -> new ForbiddenException("No building assigned"));
        BookingRequest br = bookingRepo.findById(bookingRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking request not found"));
        if (!br.getBuildingId().equals(building.getId())) {
            throw new ForbiddenException("Booking not in your building");
        }
        if (br.getStatus() == BookingStatus.ALLOCATED) {
            throw new ConflictException("Booking already allocated");
        }
        if (br.getStatus() == BookingStatus.REJECTED || br.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking is " + br.getStatus());
        }

        TenantProfile tp = tenantRepo.findByUserId(br.getTenantUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant profile not found"));
        if (tp.getCurrentBedId() != null) {
            throw new ConflictException("Tenant already has a bed allocated");
        }

        Bed bed = bedRepo.findByIdForUpdate(bedId)
                .orElseThrow(() -> new ResourceNotFoundException("Bed not found"));
        if (!bed.getBuildingId().equals(building.getId())) {
            throw new ForbiddenException("Bed not in your building");
        }
        if (bed.getStatus() != BedStatus.AVAILABLE) {
            throw new ConflictException("Bed is not available");
        }
        Room room = roomRepo.findById(bed.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        if (room.getSharingType() != br.getSharingType()) {
            throw new BadRequestException("Bed sharing type does not match request");
        }

        bed.setStatus(BedStatus.OCCUPIED);
        bed.setTenantUserId(br.getTenantUserId());
        bedRepo.save(bed);

        tp.setCurrentBedId(bed.getId());
        tp.setCurrentBuildingId(building.getId());
        tenantRepo.save(tp);

        br.setStatus(BookingStatus.ALLOCATED);
        br.setAllocatedBedId(bed.getId());
        br.setDecidedAt(Instant.now());
        bookingRepo.save(br);

        SharingConfig sc = sharingRepo.findByBuildingIdAndSharingType(building.getId(), br.getSharingType())
                .orElseThrow(() -> new ResourceNotFoundException("Sharing config not found"));
        BigDecimal amount = room.getMonthlyRent() != null ? room.getMonthlyRent() : sc.getMonthlyRent();

        String ym = YearMonth.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        if (paymentRepo.findByTenantUserIdAndMonthYear(br.getTenantUserId(), ym).isEmpty()) {
            Payment p = new Payment();
            p.setTenantUserId(br.getTenantUserId());
            p.setBuildingId(building.getId());
            p.setBedId(bed.getId());
            p.setMonthYear(ym);
            p.setAmount(amount);
            p.setStatus(PaymentStatus.UNPAID);
            paymentRepo.save(p);
        }

        BookingRequestResponseDto dto = BookingRequestResponseDto.of(br);
        dto.setBuildingName(building.getName());
        dto.setAllocatedBedNumber(bed.getBedNumber());
        dto.setAllocatedRoomNumber(room.getRoomNumber());
        userRepo.findById(br.getTenantUserId()).ifPresent(u -> {
            dto.setTenantName(u.getFullName());
            dto.setTenantEmail(u.getEmail());
            dto.setTenantPhone(u.getPhone());
        });
        return dto;
    }

    @Transactional
    public ApiMessage checkout(Long receptionistUserId, Long tenantUserId) {
        Building building = buildingRepo.findByReceptionistUserId(receptionistUserId)
                .orElseThrow(() -> new ForbiddenException("No building assigned"));
        TenantProfile tp = tenantRepo.findByUserId(tenantUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
        if (tp.getCurrentBedId() == null) {
            throw new BadRequestException("Tenant has no active allocation");
        }
        Bed bed = bedRepo.findByIdForUpdate(tp.getCurrentBedId())
                .orElseThrow(() -> new ResourceNotFoundException("Bed not found"));
        if (!bed.getBuildingId().equals(building.getId())) {
            throw new ForbiddenException("Tenant not in your building");
        }
        bed.setStatus(BedStatus.AVAILABLE);
        bed.setTenantUserId(null);
        bedRepo.save(bed);

        tp.setCurrentBedId(null);
        tp.setCurrentBuildingId(null);
        tenantRepo.save(tp);

        for (BookingRequest br : bookingRepo.findByTenantUserIdAndStatus(tenantUserId, BookingStatus.ALLOCATED)) {
            if (building.getId().equals(br.getBuildingId())) {
                br.setStatus(BookingStatus.CANCELLED);
                br.setDecisionNote("Tenant checked out");
                br.setDecidedAt(Instant.now());
                bookingRepo.save(br);
            }
        }
        return new ApiMessage("Tenant checked out");
    }

    @Transactional(readOnly = true)
    public java.util.List<com.example.demo.dto.response.BedDto> availableBedsForSharing(Long receptionistUserId,
                                                                                       Long buildingId,
                                                                                       com.example.demo.enums.SharingType sharingType) {
        Building building = buildingRepo.findByReceptionistUserId(receptionistUserId)
                .orElseThrow(() -> new ForbiddenException("No building assigned"));
        if (!building.getId().equals(buildingId)) {
            throw new ForbiddenException("Not your building");
        }
        java.util.List<Bed> beds = bedRepo.findAvailableByBuildingAndSharing(buildingId, sharingType);
        java.util.List<com.example.demo.dto.response.BedDto> out = new java.util.ArrayList<>();
        for (Bed b : beds) {
            com.example.demo.dto.response.BedDto d = com.example.demo.dto.response.BedDto.of(b);
            roomRepo.findById(b.getRoomId()).ifPresent(r -> d.setBedNumber(r.getRoomNumber() + " / " + b.getBedNumber()));
            out.add(d);
        }
        return out;
    }
}
