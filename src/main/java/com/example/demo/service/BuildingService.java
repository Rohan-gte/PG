package com.example.demo.service;

import com.example.demo.dto.request.AddRoomRequest;
import com.example.demo.dto.request.CreateBuildingRequest;
import com.example.demo.dto.request.ReceptionistRequest;
import com.example.demo.dto.request.SharingConfigDto;
import com.example.demo.dto.request.UpdateBuildingRequest;
import com.example.demo.dto.request.UpdateRoomRequest;
import com.example.demo.dto.response.ApiMessage;
import com.example.demo.dto.response.BuildingDto;
import com.example.demo.dto.response.RoomDto;
import com.example.demo.entity.Bed;
import com.example.demo.entity.Building;
import com.example.demo.entity.BuildingImage;
import com.example.demo.entity.Room;
import com.example.demo.entity.SharingConfig;
import com.example.demo.entity.User;
import com.example.demo.enums.BedStatus;
import com.example.demo.enums.Role;
import com.example.demo.enums.SharingType;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BedRepository;
import com.example.demo.repository.BuildingImageRepository;
import com.example.demo.repository.BuildingRepository;
import com.example.demo.repository.RoomRepository;
import com.example.demo.repository.SharingConfigRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BuildingService {

    private final BuildingRepository buildingRepo;
    private final BuildingImageRepository imageRepo;
    private final SharingConfigRepository sharingRepo;
    private final RoomRepository roomRepo;
    private final BedRepository bedRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final BuildingMapper buildingMapper;

    public BuildingService(BuildingRepository buildingRepo,
                           BuildingImageRepository imageRepo,
                           SharingConfigRepository sharingRepo,
                           RoomRepository roomRepo,
                           BedRepository bedRepo,
                           UserRepository userRepo,
                           PasswordEncoder passwordEncoder,
                           BuildingMapper buildingMapper) {
        this.buildingRepo = buildingRepo;
        this.imageRepo = imageRepo;
        this.sharingRepo = sharingRepo;
        this.roomRepo = roomRepo;
        this.bedRepo = bedRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.buildingMapper = buildingMapper;
    }

    @Transactional
    public BuildingDto createBuilding(Long ownerUserId, CreateBuildingRequest req) {
        if (req.getSharingConfigs() == null || req.getSharingConfigs().isEmpty()) {
            throw new BadRequestException("At least one sharing configuration is required");
        }
        long distinct = req.getSharingConfigs().stream().map(SharingConfigDto::getSharingType).distinct().count();
        if (distinct != req.getSharingConfigs().size()) {
            throw new BadRequestException("Duplicate sharing types in configs");
        }

        ReceptionistRequest rr = req.getReceptionist();
        if (rr == null) throw new BadRequestException("Receptionist details are required");
        if (userRepo.existsByEmailIgnoreCase(rr.getEmail())) {
            throw new ConflictException("Receptionist email already in use");
        }

        Building b = new Building();
        b.setOwnerUserId(ownerUserId);
        b.setName(req.getName());
        b.setAddress(req.getAddress());
        b.setArea(req.getArea());
        b.setCity(req.getCity());
        b.setDescription(req.getDescription());
        b.setAmenitiesCsv(joinCsv(req.getAmenities()));
        b.setTotalFloors(req.getTotalFloors());
        b.setContactPhone(req.getContactPhone());
        b.setContactEmail(req.getContactEmail());
        buildingRepo.save(b);

        User rec = new User();
        rec.setFullName(rr.getFullName());
        rec.setEmail(rr.getEmail().toLowerCase());
        rec.setPhone(rr.getPhone());
        rec.setPasswordHash(passwordEncoder.encode(rr.getPassword()));
        rec.setRole(Role.RECEPTIONIST);
        rec.setEnabled(true);
        userRepo.save(rec);
        b.setReceptionistUserId(rec.getId());
        buildingRepo.save(b);

        if (req.getImagePaths() != null) {
            int idx = 0;
            for (String p : req.getImagePaths()) {
                if (p == null || p.isBlank()) continue;
                BuildingImage img = new BuildingImage();
                img.setBuildingId(b.getId());
                img.setPath(p);
                img.setSortOrder(idx++);
                imageRepo.save(img);
            }
        }

        for (SharingConfigDto cfg : req.getSharingConfigs()) {
            SharingConfig sc = new SharingConfig();
            sc.setBuildingId(b.getId());
            sc.setSharingType(cfg.getSharingType());
            sc.setNumRooms(cfg.getNumRooms());
            sc.setBedsPerRoom(cfg.getBedsPerRoom());
            sc.setMonthlyRent(cfg.getMonthlyRent());
            sc.setDepositAmount(cfg.getDepositAmount());
            sharingRepo.save(sc);

            generateRoomsAndBeds(b, sc);
        }

        return buildingMapper.toDto(b);
    }

    private void generateRoomsAndBeds(Building b, SharingConfig sc) {
        int floors = b.getTotalFloors() == null || b.getTotalFloors() < 1 ? 1 : b.getTotalFloors();
        long existingRoomCount = roomRepo.countByBuildingId(b.getId());
        char prefix = (char) ('A' + (sc.getSharingType().ordinal()));

        for (int i = 1; i <= sc.getNumRooms(); i++) {
            int floor = ((i - 1) % floors) + 1;
            int seq = (int) existingRoomCount + i;
            String roomNumber = prefix + String.format("%d%02d", floor, seq);

            Room r = new Room();
            r.setBuildingId(b.getId());
            r.setSharingType(sc.getSharingType());
            r.setFloorNumber(floor);
            r.setRoomNumber(roomNumber);
            r.setTotalBeds(sc.getBedsPerRoom());
            r.setMonthlyRent(sc.getMonthlyRent());
            r.setDepositAmount(sc.getDepositAmount());
            roomRepo.save(r);

            for (int bn = 1; bn <= sc.getBedsPerRoom(); bn++) {
                Bed bed = new Bed();
                bed.setRoomId(r.getId());
                bed.setBuildingId(b.getId());
                bed.setBedNumber("B" + bn);
                bed.setStatus(BedStatus.AVAILABLE);
                bedRepo.save(bed);
            }
        }
    }

    @Transactional
    public BuildingDto updateBuilding(Long ownerUserId, Long buildingId, UpdateBuildingRequest req) {
        Building b = mustOwn(ownerUserId, buildingId);
        b.setName(req.getName());
        b.setAddress(req.getAddress());
        b.setArea(req.getArea());
        b.setCity(req.getCity());
        b.setDescription(req.getDescription());
        b.setAmenitiesCsv(joinCsv(req.getAmenities()));
        b.setTotalFloors(req.getTotalFloors());
        b.setContactPhone(req.getContactPhone());
        b.setContactEmail(req.getContactEmail());
        buildingRepo.save(b);

        if (req.getImagePaths() != null) {
            imageRepo.deleteByBuildingId(buildingId);
            int idx = 0;
            for (String p : req.getImagePaths()) {
                if (p == null || p.isBlank()) continue;
                BuildingImage img = new BuildingImage();
                img.setBuildingId(buildingId);
                img.setPath(p);
                img.setSortOrder(idx++);
                imageRepo.save(img);
            }
        }
        return buildingMapper.toDto(b);
    }

    @Transactional
    public ApiMessage deleteBuilding(Long ownerUserId, Long buildingId) {
        Building b = mustOwn(ownerUserId, buildingId);
        long occupied = bedRepo.countByBuildingIdAndStatus(b.getId(), BedStatus.OCCUPIED);
        if (occupied > 0) {
            throw new ConflictException("Cannot delete building with " + occupied + " occupied bed(s)");
        }
        bedRepo.deleteByBuildingId(buildingId);
        roomRepo.deleteByBuildingId(buildingId);
        sharingRepo.deleteByBuildingId(buildingId);
        imageRepo.deleteByBuildingId(buildingId);

        if (b.getReceptionistUserId() != null) {
            Long rid = b.getReceptionistUserId();
            b.setReceptionistUserId(null);
            buildingRepo.save(b);
            userRepo.deleteById(rid);
        }
        buildingRepo.delete(b);
        return new ApiMessage("Building deleted");
    }

    @Transactional(readOnly = true)
    public List<BuildingDto> listForOwner(Long ownerUserId) {
        return buildingRepo.findByOwnerUserId(ownerUserId).stream()
                .map(b -> buildingMapper.toDto(b, true))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BuildingDto getForOwner(Long ownerUserId, Long buildingId) {
        return buildingMapper.toDto(mustOwn(ownerUserId, buildingId));
    }

    @Transactional(readOnly = true)
    public List<RoomDto> roomsForOwner(Long ownerUserId, Long buildingId) {
        mustOwn(ownerUserId, buildingId);
        return roomsWithCounts(buildingId);
    }

    @Transactional(readOnly = true)
    public List<RoomDto> roomsForBuilding(Long buildingId) {
        return roomsWithCounts(buildingId);
    }

    private List<RoomDto> roomsWithCounts(Long buildingId) {
        List<Room> rooms = roomRepo.findByBuildingId(buildingId);
        List<RoomDto> out = new ArrayList<>();
        for (Room r : rooms) {
            RoomDto d = RoomDto.of(r);
            List<Bed> beds = bedRepo.findByRoomId(r.getId());
            long occ = beds.stream().filter(b -> b.getStatus() == BedStatus.OCCUPIED).count();
            d.setOccupiedBeds(occ);
            d.setAvailableBeds(beds.size() - occ);
            out.add(d);
        }
        return out;
    }

    @Transactional
    public RoomDto addRoom(Long ownerUserId, Long buildingId, AddRoomRequest req) {
        Building b = mustOwn(ownerUserId, buildingId);
        SharingConfig sc = sharingRepo.findByBuildingIdAndSharingType(buildingId, req.getSharingType())
                .orElseThrow(() -> new BadRequestException("Sharing type not configured for this building"));
        if (req.getTotalBeds() < 1) throw new BadRequestException("totalBeds must be >= 1");

        Room r = new Room();
        r.setBuildingId(b.getId());
        r.setSharingType(req.getSharingType());
        r.setFloorNumber(req.getFloorNumber());
        r.setRoomNumber(req.getRoomNumber());
        r.setTotalBeds(req.getTotalBeds());
        r.setMonthlyRent(req.getMonthlyRent() == null ? sc.getMonthlyRent() : req.getMonthlyRent());
        r.setDepositAmount(req.getDepositAmount() == null ? sc.getDepositAmount() : req.getDepositAmount());
        roomRepo.save(r);
        for (int i = 1; i <= req.getTotalBeds(); i++) {
            Bed bed = new Bed();
            bed.setRoomId(r.getId());
            bed.setBuildingId(b.getId());
            bed.setBedNumber("B" + i);
            bed.setStatus(BedStatus.AVAILABLE);
            bedRepo.save(bed);
        }
        return RoomDto.of(r);
    }

    @Transactional
    public RoomDto updateRoom(Long ownerUserId, Long roomId, UpdateRoomRequest req) {
        Room r = roomRepo.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        mustOwn(ownerUserId, r.getBuildingId());
        r.setMonthlyRent(req.getMonthlyRent());
        r.setDepositAmount(req.getDepositAmount());
        if (req.getFloorNumber() != null) r.setFloorNumber(req.getFloorNumber());
        roomRepo.save(r);
        return RoomDto.of(r);
    }

    @Transactional
    public ApiMessage deleteRoom(Long ownerUserId, Long roomId) {
        Room r = roomRepo.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        mustOwn(ownerUserId, r.getBuildingId());
        List<Bed> beds = bedRepo.findByRoomId(roomId);
        long occ = beds.stream().filter(b -> b.getStatus() == BedStatus.OCCUPIED).count();
        if (occ > 0) {
            throw new ConflictException("Cannot delete room with " + occ + " occupied bed(s)");
        }
        bedRepo.deleteByRoomId(roomId);
        roomRepo.delete(r);
        return new ApiMessage("Room deleted");
    }

    private Building mustOwn(Long ownerUserId, Long buildingId) {
        Building b = buildingRepo.findById(buildingId)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found"));
        if (!b.getOwnerUserId().equals(ownerUserId)) {
            throw new ForbiddenException("Not your building");
        }
        return b;
    }

    private String joinCsv(List<String> items) {
        if (items == null || items.isEmpty()) return "";
        return items.stream().map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.joining(","));
    }
}
