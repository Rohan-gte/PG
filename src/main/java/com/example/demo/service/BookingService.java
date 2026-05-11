package com.example.demo.service;

import com.example.demo.dto.request.BookingRequestDto;
import com.example.demo.dto.response.ApiMessage;
import com.example.demo.dto.response.BookingRequestResponseDto;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.entity.Bed;
import com.example.demo.entity.BookingRequest;
import com.example.demo.entity.Building;
import com.example.demo.entity.Room;
import com.example.demo.enums.BookingStatus;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BedRepository;
import com.example.demo.repository.BookingRequestRepository;
import com.example.demo.repository.BuildingRepository;
import com.example.demo.repository.RoomRepository;
import com.example.demo.repository.TenantProfileRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class BookingService {

    private final BookingRequestRepository bookingRepo;
    private final BuildingRepository buildingRepo;
    private final UserRepository userRepo;
    private final BedRepository bedRepo;
    private final RoomRepository roomRepo;
    private final TenantProfileRepository tenantRepo;

    public BookingService(BookingRequestRepository bookingRepo,
                          BuildingRepository buildingRepo,
                          UserRepository userRepo,
                          BedRepository bedRepo,
                          RoomRepository roomRepo,
                          TenantProfileRepository tenantRepo) {
        this.bookingRepo = bookingRepo;
        this.buildingRepo = buildingRepo;
        this.userRepo = userRepo;
        this.bedRepo = bedRepo;
        this.roomRepo = roomRepo;
        this.tenantRepo = tenantRepo;
    }

    @Transactional
    public BookingRequestResponseDto submit(Long tenantUserId, BookingRequestDto req) {
        if (req.getBuildingId() == null || req.getSharingType() == null) {
            throw new BadRequestException("buildingId and sharingType are required");
        }
        Building b = buildingRepo.findById(req.getBuildingId())
                .orElseThrow(() -> new ResourceNotFoundException("Building not found"));
        boolean tenantHasBed = tenantRepo.findByUserId(tenantUserId)
                .map(t -> t.getCurrentBedId() != null).orElse(false);
        if (tenantHasBed) {
            throw new ConflictException("You already have an active allocation");
        }
        boolean openExists = bookingRepo.findByTenantUserIdAndStatus(tenantUserId, BookingStatus.PENDING).stream()
                .anyMatch(x -> x.getBuildingId().equals(b.getId()));
        if (openExists) {
            throw new ConflictException("You already have a pending request for this building");
        }
        BookingRequest br = new BookingRequest();
        br.setTenantUserId(tenantUserId);
        br.setBuildingId(b.getId());
        br.setSharingType(req.getSharingType());
        br.setStatus(BookingStatus.PENDING);
        br.setNotes(req.getNotes());
        bookingRepo.save(br);
        return hydrate(br);
    }

    @Transactional
    public ApiMessage cancel(Long tenantUserId, Long bookingId) {
        BookingRequest br = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
        if (!br.getTenantUserId().equals(tenantUserId)) {
            throw new ForbiddenException("Not your booking request");
        }
        if (br.getStatus() != BookingStatus.PENDING && br.getStatus() != BookingStatus.APPROVED) {
            throw new BadRequestException("Cannot cancel a " + br.getStatus() + " request");
        }
        br.setStatus(BookingStatus.CANCELLED);
        br.setDecidedAt(Instant.now());
        bookingRepo.save(br);
        return new ApiMessage("Request cancelled");
    }

    @Transactional
    public BookingRequestResponseDto approve(Long receptionistUserId, Long bookingId) {
        BookingRequest br = mustBeReceptionistBuilding(receptionistUserId, bookingId);
        if (br.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Only PENDING requests can be approved");
        }
        br.setStatus(BookingStatus.APPROVED);
        br.setDecidedAt(Instant.now());
        bookingRepo.save(br);
        return hydrate(br);
    }

    @Transactional
    public BookingRequestResponseDto reject(Long receptionistUserId, Long bookingId, String reason) {
        BookingRequest br = mustBeReceptionistBuilding(receptionistUserId, bookingId);
        if (br.getStatus() != BookingStatus.PENDING && br.getStatus() != BookingStatus.APPROVED) {
            throw new BadRequestException("Cannot reject " + br.getStatus() + " request");
        }
        br.setStatus(BookingStatus.REJECTED);
        br.setDecisionNote(reason);
        br.setDecidedAt(Instant.now());
        bookingRepo.save(br);
        return hydrate(br);
    }

    @Transactional(readOnly = true)
    public PageResponse<BookingRequestResponseDto> listForReceptionist(Long receptionistUserId,
                                                                      BookingStatus status,
                                                                      Pageable pageable) {
        Building building = buildingRepo.findByReceptionistUserId(receptionistUserId)
                .orElseThrow(() -> new ForbiddenException("No building assigned"));
        Page<BookingRequest> page = status == null
                ? bookingRepo.findByBuildingId(building.getId(), pageable)
                : bookingRepo.findByBuildingIdAndStatus(building.getId(), status, pageable);
        return PageResponse.of(page, this::hydrate);
    }

    @Transactional(readOnly = true)
    public PageResponse<BookingRequestResponseDto> listForTenant(Long tenantUserId, Pageable pageable) {
        Page<BookingRequest> page = bookingRepo.findByTenantUserId(tenantUserId, pageable);
        return PageResponse.of(page, this::hydrate);
    }

    private BookingRequest mustBeReceptionistBuilding(Long receptionistUserId, Long bookingId) {
        Building building = buildingRepo.findByReceptionistUserId(receptionistUserId)
                .orElseThrow(() -> new ForbiddenException("No building assigned"));
        BookingRequest br = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (!br.getBuildingId().equals(building.getId())) {
            throw new ForbiddenException("Booking not in your building");
        }
        return br;
    }

    public BookingRequestResponseDto hydrate(BookingRequest br) {
        BookingRequestResponseDto d = BookingRequestResponseDto.of(br);
        userRepo.findById(br.getTenantUserId()).ifPresent(u -> {
            d.setTenantName(u.getFullName());
            d.setTenantEmail(u.getEmail());
            d.setTenantPhone(u.getPhone());
        });
        buildingRepo.findById(br.getBuildingId()).ifPresent(b -> d.setBuildingName(b.getName()));
        if (br.getAllocatedBedId() != null) {
            Bed bed = bedRepo.findById(br.getAllocatedBedId()).orElse(null);
            if (bed != null) {
                d.setAllocatedBedNumber(bed.getBedNumber());
                Room r = roomRepo.findById(bed.getRoomId()).orElse(null);
                if (r != null) d.setAllocatedRoomNumber(r.getRoomNumber());
            }
        }
        return d;
    }
}
