package com.example.demo.service;

import com.example.demo.dto.response.BuildingDto;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.entity.Building;
import com.example.demo.entity.SharingConfig;
import com.example.demo.enums.BedStatus;
import com.example.demo.enums.SharingType;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BedRepository;
import com.example.demo.repository.BuildingRepository;
import com.example.demo.repository.SharingConfigRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BuildingBrowseService {

    private final BuildingRepository buildingRepo;
    private final SharingConfigRepository sharingRepo;
    private final BedRepository bedRepo;
    private final BuildingMapper buildingMapper;

    public BuildingBrowseService(BuildingRepository buildingRepo,
                                 SharingConfigRepository sharingRepo,
                                 BedRepository bedRepo,
                                 BuildingMapper buildingMapper) {
        this.buildingRepo = buildingRepo;
        this.sharingRepo = sharingRepo;
        this.bedRepo = bedRepo;
        this.buildingMapper = buildingMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<BuildingDto> search(String q,
                                            String city,
                                            String area,
                                            SharingType sharingType,
                                            BigDecimal maxRent,
                                            Boolean availableOnly,
                                            Pageable pageable) {
        Page<Building> page = buildingRepo.search(q, pageable);
        List<Building> filtered = new ArrayList<>();
        for (Building b : page.getContent()) {
            if (city != null && !city.isBlank() && (b.getCity() == null || !b.getCity().equalsIgnoreCase(city))) continue;
            if (area != null && !area.isBlank() && (b.getArea() == null || !b.getArea().toLowerCase().contains(area.toLowerCase()))) continue;
            if (sharingType != null) {
                SharingConfig sc = sharingRepo.findByBuildingIdAndSharingType(b.getId(), sharingType).orElse(null);
                if (sc == null) continue;
                if (maxRent != null && sc.getMonthlyRent().compareTo(maxRent) > 0) continue;
                if (Boolean.TRUE.equals(availableOnly)) {
                    long avail = bedRepo.countByBuildingAndSharing(b.getId(), sharingType)
                            - bedRepo.countByBuildingAndSharingAndStatus(b.getId(), sharingType, BedStatus.OCCUPIED);
                    if (avail <= 0) continue;
                }
            } else {
                if (maxRent != null) {
                    boolean any = sharingRepo.findByBuildingId(b.getId()).stream()
                            .anyMatch(sc -> sc.getMonthlyRent().compareTo(maxRent) <= 0);
                    if (!any) continue;
                }
                if (Boolean.TRUE.equals(availableOnly)) {
                    long total = bedRepo.countByBuildingId(b.getId());
                    long occ = bedRepo.countByBuildingIdAndStatus(b.getId(), BedStatus.OCCUPIED);
                    if (total - occ <= 0) continue;
                }
            }
            filtered.add(b);
        }
        List<BuildingDto> dtos = filtered.stream().map(b -> buildingMapper.toDto(b, true)).collect(Collectors.toList());
        Page<BuildingDto> resultPage = new PageImpl<>(dtos, pageable, page.getTotalElements());
        return PageResponse.of(resultPage, x -> x);
    }

    @Transactional(readOnly = true)
    public BuildingDto getById(Long id) {
        Building b = buildingRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found"));
        return buildingMapper.toDto(b);
    }
}
