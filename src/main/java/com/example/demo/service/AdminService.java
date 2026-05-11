package com.example.demo.service;

import com.example.demo.dto.response.ApiMessage;
import com.example.demo.dto.response.BuildingDto;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.dto.response.UserDto;
import com.example.demo.entity.Building;
import com.example.demo.entity.OwnerProfile;
import com.example.demo.entity.User;
import com.example.demo.enums.OwnerStatus;
import com.example.demo.enums.Role;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BuildingRepository;
import com.example.demo.repository.OwnerProfileRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final UserRepository userRepo;
    private final OwnerProfileRepository ownerRepo;
    private final BuildingRepository buildingRepo;
    private final BuildingMapper buildingMapper;

    public AdminService(UserRepository userRepo,
                        OwnerProfileRepository ownerRepo,
                        BuildingRepository buildingRepo,
                        BuildingMapper buildingMapper) {
        this.userRepo = userRepo;
        this.ownerRepo = ownerRepo;
        this.buildingRepo = buildingRepo;
        this.buildingMapper = buildingMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserDto> listOwners(OwnerStatus status, Pageable pageable) {
        Page<OwnerProfile> page = status == null
                ? ownerRepo.findAll(pageable)
                : ownerRepo.findByStatus(status, pageable);
        return PageResponse.of(page, op -> {
            User u = userRepo.findById(op.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User missing for owner"));
            UserDto d = UserDto.of(u);
            d.setOwnerStatus(op.getStatus());
            return d;
        });
    }

    @Transactional
    public ApiMessage approveOwner(Long ownerUserId) {
        OwnerProfile op = ownerRepo.findByUserId(ownerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));
        if (op.getStatus() == OwnerStatus.APPROVED) {
            throw new BadRequestException("Owner already approved");
        }
        op.setStatus(OwnerStatus.APPROVED);
        op.setRejectionReason(null);
        ownerRepo.save(op);
        User u = userRepo.findById(ownerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        u.setEnabled(true);
        userRepo.save(u);
        return new ApiMessage("Owner approved");
    }

    @Transactional
    public ApiMessage rejectOwner(Long ownerUserId, String reason) {
        OwnerProfile op = ownerRepo.findByUserId(ownerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));
        op.setStatus(OwnerStatus.REJECTED);
        op.setRejectionReason(reason);
        ownerRepo.save(op);
        User u = userRepo.findById(ownerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        u.setEnabled(false);
        userRepo.save(u);
        return new ApiMessage("Owner rejected");
    }

    @Transactional(readOnly = true)
    public PageResponse<UserDto> listByRole(Role role, String q, Pageable pageable) {
        Page<User> page = (q == null || q.isBlank())
                ? userRepo.findByRole(role, pageable)
                : userRepo.searchByRole(role, q, pageable);
        return PageResponse.of(page, u -> {
            UserDto d = UserDto.of(u);
            if (role == Role.RECEPTIONIST) {
                buildingRepo.findByReceptionistUserId(u.getId())
                        .ifPresent(b -> d.setBuildingId(b.getId()));
            }
            return d;
        });
    }

    @Transactional(readOnly = true)
    public PageResponse<BuildingDto> listBuildings(String q, Pageable pageable) {
        Page<Building> page = buildingRepo.search(q, pageable);
        return PageResponse.of(page, b -> buildingMapper.toDto(b, true));
    }
}
