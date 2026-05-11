package com.example.demo.service;

import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.RegisterOwnerRequest;
import com.example.demo.dto.request.RegisterTenantRequest;
import com.example.demo.dto.response.AuthResponse;
import com.example.demo.dto.response.UserDto;
import com.example.demo.entity.Bed;
import com.example.demo.entity.Building;
import com.example.demo.entity.OwnerProfile;
import com.example.demo.entity.TenantProfile;
import com.example.demo.entity.User;
import com.example.demo.enums.OwnerStatus;
import com.example.demo.enums.Role;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BedRepository;
import com.example.demo.repository.BuildingRepository;
import com.example.demo.repository.OwnerProfileRepository;
import com.example.demo.repository.TenantProfileRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final OwnerProfileRepository ownerRepo;
    private final TenantProfileRepository tenantRepo;
    private final BuildingRepository buildingRepo;
    private final BedRepository bedRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepo,
                       OwnerProfileRepository ownerRepo,
                       TenantProfileRepository tenantRepo,
                       BuildingRepository buildingRepo,
                       BedRepository bedRepo,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.ownerRepo = ownerRepo;
        this.tenantRepo = tenantRepo;
        this.buildingRepo = buildingRepo;
        this.bedRepo = bedRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public UserDto registerTenant(RegisterTenantRequest req) {
        if (userRepo.existsByEmailIgnoreCase(req.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        User u = new User();
        u.setFullName(req.getFullName());
        u.setEmail(req.getEmail().toLowerCase());
        u.setPhone(req.getPhone());
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        u.setRole(Role.TENANT);
        u.setEnabled(true);
        userRepo.save(u);

        TenantProfile tp = new TenantProfile();
        tp.setUser(u);
        tp.setGender(req.getGender());
        tp.setIdProofPath(req.getIdProofPath());
        tenantRepo.save(tp);

        return enrich(UserDto.of(u));
    }

    @Transactional
    public UserDto registerOwner(RegisterOwnerRequest req) {
        if (userRepo.existsByEmailIgnoreCase(req.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        User u = new User();
        u.setFullName(req.getFullName());
        u.setEmail(req.getEmail().toLowerCase());
        u.setPhone(req.getPhone());
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        u.setRole(Role.OWNER);
        u.setEnabled(false);
        userRepo.save(u);

        OwnerProfile op = new OwnerProfile();
        op.setUser(u);
        op.setAddress(req.getAddress());
        op.setStatus(OwnerStatus.PENDING);
        ownerRepo.save(op);

        UserDto dto = UserDto.of(u);
        dto.setOwnerStatus(OwnerStatus.PENDING);
        return dto;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User u = userRepo.findByEmailIgnoreCase(req.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(req.getPassword(), u.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        if (u.getRole() == Role.OWNER) {
            OwnerProfile op = ownerRepo.findByUserId(u.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Owner profile missing"));
            if (op.getStatus() == OwnerStatus.PENDING) {
                throw new ForbiddenException("Owner registration pending admin approval");
            }
            if (op.getStatus() == OwnerStatus.REJECTED) {
                throw new ForbiddenException("Owner registration was rejected" +
                        (op.getRejectionReason() == null ? "" : ": " + op.getRejectionReason()));
            }
        }
        if (!u.isEnabled()) {
            throw new ForbiddenException("Account disabled");
        }
        String token = jwtUtil.generate(u.getId(), u.getEmail(), u.getRole().name());
        return new AuthResponse(token, jwtUtil.getExpirationMs(), enrich(UserDto.of(u)));
    }

    @Transactional(readOnly = true)
    public UserDto me(Long userId) {
        User u = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return enrich(UserDto.of(u));
    }

    private UserDto enrich(UserDto dto) {
        if (dto == null) return null;
        if (dto.getRole() == Role.OWNER) {
            ownerRepo.findByUserId(dto.getId()).ifPresent(op -> dto.setOwnerStatus(op.getStatus()));
        } else if (dto.getRole() == Role.RECEPTIONIST) {
            buildingRepo.findByReceptionistUserId(dto.getId())
                    .ifPresent(b -> dto.setBuildingId(b.getId()));
        } else if (dto.getRole() == Role.TENANT) {
            tenantRepo.findByUserId(dto.getId()).ifPresent(tp -> {
                if (tp.getCurrentBuildingId() != null) {
                    dto.setBuildingId(tp.getCurrentBuildingId());
                } else {
                    Bed b = bedRepo.findByTenantUserId(dto.getId()).orElse(null);
                    if (b != null) dto.setBuildingId(b.getBuildingId());
                }
            });
        }
        return dto;
    }
}
