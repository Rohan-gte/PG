package com.example.demo.service;

import com.example.demo.dto.response.BedDto;
import com.example.demo.dto.response.BuildingDto;
import com.example.demo.dto.response.RoomDto;
import com.example.demo.dto.response.UserDto;
import com.example.demo.entity.Bed;
import com.example.demo.entity.Building;
import com.example.demo.entity.Room;
import com.example.demo.entity.TenantProfile;
import com.example.demo.entity.User;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BedRepository;
import com.example.demo.repository.BuildingRepository;
import com.example.demo.repository.RoomRepository;
import com.example.demo.repository.TenantProfileRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReceptionistService {

    private final BuildingRepository buildingRepo;
    private final RoomRepository roomRepo;
    private final BedRepository bedRepo;
    private final TenantProfileRepository tenantRepo;
    private final UserRepository userRepo;
    private final BuildingMapper buildingMapper;

    public ReceptionistService(BuildingRepository buildingRepo,
                               RoomRepository roomRepo,
                               BedRepository bedRepo,
                               TenantProfileRepository tenantRepo,
                               UserRepository userRepo,
                               BuildingMapper buildingMapper) {
        this.buildingRepo = buildingRepo;
        this.roomRepo = roomRepo;
        this.bedRepo = bedRepo;
        this.tenantRepo = tenantRepo;
        this.userRepo = userRepo;
        this.buildingMapper = buildingMapper;
    }

    @Transactional(readOnly = true)
    public BuildingDto myBuilding(Long receptionistUserId) {
        Building b = buildingRepo.findByReceptionistUserId(receptionistUserId)
                .orElseThrow(() -> new ForbiddenException("No building assigned"));
        return buildingMapper.toDto(b);
    }

    @Transactional(readOnly = true)
    public List<RoomDto> rooms(Long receptionistUserId) {
        Building b = buildingRepo.findByReceptionistUserId(receptionistUserId)
                .orElseThrow(() -> new ForbiddenException("No building assigned"));
        List<Room> rooms = roomRepo.findByBuildingId(b.getId());
        List<RoomDto> out = new ArrayList<>();
        for (Room r : rooms) {
            RoomDto d = RoomDto.of(r);
            List<Bed> beds = bedRepo.findByRoomId(r.getId());
            long occ = beds.stream().filter(x -> x.getStatus() == com.example.demo.enums.BedStatus.OCCUPIED).count();
            d.setOccupiedBeds(occ);
            d.setAvailableBeds(beds.size() - occ);
            List<BedDto> bedDtos = new ArrayList<>();
            for (Bed bed : beds) {
                BedDto bd = BedDto.of(bed);
                if (bed.getTenantUserId() != null) {
                    userRepo.findById(bed.getTenantUserId()).ifPresent(u -> bd.setTenantName(u.getFullName()));
                }
                bedDtos.add(bd);
            }
            d.setBeds(bedDtos);
            out.add(d);
        }
        return out;
    }

    @Transactional(readOnly = true)
    public List<UserDto> tenants(Long receptionistUserId) {
        Building b = buildingRepo.findByReceptionistUserId(receptionistUserId)
                .orElseThrow(() -> new ForbiddenException("No building assigned"));
        List<UserDto> out = new ArrayList<>();
        for (TenantProfile tp : tenantRepo.findByCurrentBuildingId(b.getId())) {
            User u = userRepo.findById(tp.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tenant user missing"));
            UserDto d = UserDto.of(u);
            d.setBuildingId(b.getId());
            out.add(d);
        }
        return out;
    }
}
