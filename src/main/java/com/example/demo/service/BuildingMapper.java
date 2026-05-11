package com.example.demo.service;

import com.example.demo.dto.response.AvailabilityDto;
import com.example.demo.dto.response.BuildingDto;
import com.example.demo.dto.response.SharingConfigResponseDto;
import com.example.demo.entity.Building;
import com.example.demo.entity.BuildingImage;
import com.example.demo.entity.SharingConfig;
import com.example.demo.enums.BedStatus;
import com.example.demo.repository.BedRepository;
import com.example.demo.repository.BuildingImageRepository;
import com.example.demo.repository.SharingConfigRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BuildingMapper {

    private final UserRepository userRepo;
    private final BuildingImageRepository imageRepo;
    private final SharingConfigRepository sharingRepo;
    private final BedRepository bedRepo;
    private final AnalyticsService analyticsService;

    public BuildingMapper(UserRepository userRepo,
                          BuildingImageRepository imageRepo,
                          SharingConfigRepository sharingRepo,
                          BedRepository bedRepo,
                          AnalyticsService analyticsService) {
        this.userRepo = userRepo;
        this.imageRepo = imageRepo;
        this.sharingRepo = sharingRepo;
        this.bedRepo = bedRepo;
        this.analyticsService = analyticsService;
    }

    public BuildingDto toDto(Building b) {
        return toDto(b, true);
    }

    public BuildingDto toDto(Building b, boolean withAvailability) {
        BuildingDto d = BuildingDto.of(b);

        userRepo.findById(b.getOwnerUserId()).ifPresent(u -> d.setOwnerName(u.getFullName()));
        if (b.getReceptionistUserId() != null) {
            userRepo.findById(b.getReceptionistUserId()).ifPresent(r -> {
                d.setReceptionistName(r.getFullName());
                d.setReceptionistEmail(r.getEmail());
            });
        }

        List<String> images = imageRepo.findByBuildingIdOrderBySortOrderAsc(b.getId())
                .stream().map(BuildingImage::getPath).collect(Collectors.toList());
        d.setImagePaths(images);

        List<SharingConfig> configs = sharingRepo.findByBuildingId(b.getId());
        List<SharingConfigResponseDto> sdtos = configs.stream().map(s -> {
            SharingConfigResponseDto x = SharingConfigResponseDto.of(s);
            long total = bedRepo.countByBuildingAndSharing(b.getId(), s.getSharingType());
            long occ = bedRepo.countByBuildingAndSharingAndStatus(b.getId(), s.getSharingType(), BedStatus.OCCUPIED);
            x.setTotalBeds(total);
            x.setAvailableBeds(total - occ);
            return x;
        }).collect(Collectors.toList());
        d.setSharingConfigs(sdtos);

        if (withAvailability) {
            AvailabilityDto av = analyticsService.availabilityForBuilding(b.getId());
            d.setAvailability(av);
        }
        return d;
    }
}
