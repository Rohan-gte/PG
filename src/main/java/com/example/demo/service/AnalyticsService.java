package com.example.demo.service;

import com.example.demo.dto.response.AvailabilityDto;
import com.example.demo.dto.response.BuildingDto;
import com.example.demo.dto.response.DashboardDto;
import com.example.demo.entity.Bed;
import com.example.demo.entity.Building;
import com.example.demo.entity.Payment;
import com.example.demo.enums.BedStatus;
import com.example.demo.enums.BookingStatus;
import com.example.demo.enums.OwnerStatus;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.enums.Role;
import com.example.demo.enums.SharingType;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BedRepository;
import com.example.demo.repository.BookingRequestRepository;
import com.example.demo.repository.BuildingRepository;
import com.example.demo.repository.OwnerProfileRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.RoomRepository;
import com.example.demo.repository.TenantProfileRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Service
public class AnalyticsService {

    private final BuildingRepository buildingRepo;
    private final BedRepository bedRepo;
    private final RoomRepository roomRepo;
    private final BookingRequestRepository bookingRepo;
    private final PaymentRepository paymentRepo;
    private final UserRepository userRepo;
    private final OwnerProfileRepository ownerRepo;
    private final TenantProfileRepository tenantRepo;

    public AnalyticsService(BuildingRepository buildingRepo,
                            BedRepository bedRepo,
                            RoomRepository roomRepo,
                            BookingRequestRepository bookingRepo,
                            PaymentRepository paymentRepo,
                            UserRepository userRepo,
                            OwnerProfileRepository ownerRepo,
                            TenantProfileRepository tenantRepo) {
        this.buildingRepo = buildingRepo;
        this.bedRepo = bedRepo;
        this.roomRepo = roomRepo;
        this.bookingRepo = bookingRepo;
        this.paymentRepo = paymentRepo;
        this.userRepo = userRepo;
        this.ownerRepo = ownerRepo;
        this.tenantRepo = tenantRepo;
    }

    @Transactional(readOnly = true)
    public AvailabilityDto availabilityForBuilding(Long buildingId) {
        Building b = buildingRepo.findById(buildingId)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found"));
        AvailabilityDto a = new AvailabilityDto();
        a.setBuildingId(b.getId());
        long total = bedRepo.countByBuildingId(b.getId());
        long occupied = bedRepo.countByBuildingIdAndStatus(b.getId(), BedStatus.OCCUPIED);
        a.setTotalBeds(total);
        a.setOccupiedBeds(occupied);
        a.setAvailableBeds(total - occupied);
        for (SharingType st : SharingType.values()) {
            long t = bedRepo.countByBuildingAndSharing(b.getId(), st);
            long o = bedRepo.countByBuildingAndSharingAndStatus(b.getId(), st, BedStatus.OCCUPIED);
            a.getPerSharing().put(st.name(), new AvailabilityDto.SharingAvailability(t, o, t - o));
        }
        return a;
    }

    @Transactional(readOnly = true)
    public DashboardDto adminDashboard() {
        DashboardDto d = new DashboardDto();
        d.setTotalBuildings(buildingRepo.count());
        d.setTotalTenants(userRepo.countByRole(Role.TENANT));
        d.setTotalOwners(userRepo.countByRole(Role.OWNER));
        d.setPendingOwners(ownerRepo.countByStatus(OwnerStatus.PENDING));
        d.setTotalReceptionists(userRepo.countByRole(Role.RECEPTIONIST));
        long total = bedRepo.count();
        long occ = countAllOccupied();
        d.setTotalBeds(total);
        d.setOccupiedBeds(occ);
        d.setAvailableBeds(total - occ);
        d.setTotalRevenue(nonNull(paymentRepo.sumPaidGlobal()));
        d.setMonthRevenue(monthRevenueGlobal());
        d.setPendingPayments(paymentRepo.countByStatus(PaymentStatus.UNPAID));
        d.setOverduePayments(paymentRepo.countByStatus(PaymentStatus.OVERDUE));
        d.setBedsBySharing(bedsBySharingGlobal());
        d.setOccupancyBySharing(occupancyBySharingGlobal());
        return d;
    }

    @Transactional(readOnly = true)
    public DashboardDto ownerDashboard(Long ownerUserId) {
        DashboardDto d = new DashboardDto();
        List<Building> bs = buildingRepo.findByOwnerUserId(ownerUserId);
        d.setTotalBuildings(bs.size());
        List<BuildingDto> bDtos = new ArrayList<>();
        long totalBeds = 0, occupiedBeds = 0, pendingReq = 0;
        BigDecimal revenue = BigDecimal.ZERO;
        BigDecimal monthRev = BigDecimal.ZERO;
        long tenantCount = 0;
        for (Building b : bs) {
            long tb = bedRepo.countByBuildingId(b.getId());
            long ob = bedRepo.countByBuildingIdAndStatus(b.getId(), BedStatus.OCCUPIED);
            totalBeds += tb;
            occupiedBeds += ob;
            tenantCount += tenantRepo.countByCurrentBuildingId(b.getId());
            pendingReq += bookingRepo.countByBuildingIdAndStatus(b.getId(), BookingStatus.PENDING);
            BigDecimal sum = paymentRepo.sumPaidByBuilding(b.getId());
            revenue = revenue.add(sum == null ? BigDecimal.ZERO : sum);
            monthRev = monthRev.add(monthRevenueForBuilding(b.getId()));

            BuildingDto bd = BuildingDto.of(b);
            AvailabilityDto av = availabilityForBuilding(b.getId());
            bd.setAvailability(av);
            bDtos.add(bd);
        }
        d.setBuildings(bDtos);
        d.setTotalBeds(totalBeds);
        d.setOccupiedBeds(occupiedBeds);
        d.setAvailableBeds(totalBeds - occupiedBeds);
        d.setTotalTenants(tenantCount);
        d.setPendingRequests(pendingReq);
        d.setTotalRevenue(revenue);
        d.setMonthRevenue(monthRev);
        return d;
    }

    @Transactional(readOnly = true)
    public DashboardDto receptionistDashboard(Long receptionistUserId) {
        DashboardDto d = new DashboardDto();
        Building b = buildingRepo.findByReceptionistUserId(receptionistUserId).orElse(null);
        if (b == null) {
            return d;
        }
        long tb = bedRepo.countByBuildingId(b.getId());
        long ob = bedRepo.countByBuildingIdAndStatus(b.getId(), BedStatus.OCCUPIED);
        d.setTotalBeds(tb);
        d.setOccupiedBeds(ob);
        d.setAvailableBeds(tb - ob);
        d.setTotalTenants(tenantRepo.countByCurrentBuildingId(b.getId()));
        d.setPendingRequests(bookingRepo.countByBuildingIdAndStatus(b.getId(), BookingStatus.PENDING));
        d.setPendingPayments(paymentRepo.countByBuildingIdAndStatus(b.getId(), PaymentStatus.UNPAID));
        d.setOverduePayments(paymentRepo.countByBuildingIdAndStatus(b.getId(), PaymentStatus.OVERDUE));
        BigDecimal sum = paymentRepo.sumPaidByBuilding(b.getId());
        d.setTotalRevenue(sum == null ? BigDecimal.ZERO : sum);
        d.setMonthRevenue(monthRevenueForBuilding(b.getId()));
        d.setOccupancyBySharing(occupancyBySharingForBuilding(b.getId()));
        d.setBedsBySharing(bedsBySharingForBuilding(b.getId()));
        BuildingDto bd = BuildingDto.of(b);
        bd.setAvailability(availabilityForBuilding(b.getId()));
        d.setBuildings(List.of(bd));
        d.setTotalBuildings(1);
        return d;
    }

    @Transactional(readOnly = true)
    public DashboardDto tenantDashboard(Long tenantUserId) {
        DashboardDto d = new DashboardDto();
        Bed bed = bedRepo.findByTenantUserId(tenantUserId).orElse(null);
        if (bed != null) {
            d.setAllocatedBedId(bed.getId());
            d.setAllocatedBedNumber(bed.getBedNumber());
            roomRepo.findById(bed.getRoomId()).ifPresent(r -> d.setAllocatedRoomNumber(r.getRoomNumber()));
            buildingRepo.findById(bed.getBuildingId()).ifPresent(b -> {
                d.setAllocatedBuildingId(b.getId());
                d.setAllocatedBuildingName(b.getName());
            });
        }
        long unpaid = paymentRepo.findByTenantUserIdOrderByMonthYearDesc(tenantUserId).stream()
                .filter(p -> p.getStatus() == PaymentStatus.UNPAID).count();
        long overdue = paymentRepo.findByTenantUserIdOrderByMonthYearDesc(tenantUserId).stream()
                .filter(p -> p.getStatus() == PaymentStatus.OVERDUE).count();
        d.setPendingPayments(unpaid);
        d.setOverduePayments(overdue);
        BigDecimal paid = paymentRepo.findByTenantUserIdOrderByMonthYearDesc(tenantUserId).stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID)
                .map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        d.setTotalRevenue(paid);
        return d;
    }

    private long countAllOccupied() {
        return bedRepo.findAll().stream().filter(b -> b.getStatus() == BedStatus.OCCUPIED).count();
    }

    private LinkedHashMap<String, Long> bedsBySharingGlobal() {
        LinkedHashMap<String, Long> m = new LinkedHashMap<>();
        for (SharingType st : SharingType.values()) {
            long sum = 0;
            for (Building b : buildingRepo.findAll()) {
                sum += bedRepo.countByBuildingAndSharing(b.getId(), st);
            }
            m.put(st.name(), sum);
        }
        return m;
    }

    private LinkedHashMap<String, Long> occupancyBySharingGlobal() {
        LinkedHashMap<String, Long> m = new LinkedHashMap<>();
        for (SharingType st : SharingType.values()) {
            long sum = 0;
            for (Building b : buildingRepo.findAll()) {
                sum += bedRepo.countByBuildingAndSharingAndStatus(b.getId(), st, BedStatus.OCCUPIED);
            }
            m.put(st.name(), sum);
        }
        return m;
    }

    private LinkedHashMap<String, Long> bedsBySharingForBuilding(Long buildingId) {
        LinkedHashMap<String, Long> m = new LinkedHashMap<>();
        for (SharingType st : SharingType.values()) {
            m.put(st.name(), bedRepo.countByBuildingAndSharing(buildingId, st));
        }
        return m;
    }

    private LinkedHashMap<String, Long> occupancyBySharingForBuilding(Long buildingId) {
        LinkedHashMap<String, Long> m = new LinkedHashMap<>();
        for (SharingType st : SharingType.values()) {
            m.put(st.name(), bedRepo.countByBuildingAndSharingAndStatus(buildingId, st, BedStatus.OCCUPIED));
        }
        return m;
    }

    private BigDecimal monthRevenueGlobal() {
        String ym = YearMonth.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        return paymentRepo.findAll().stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID && ym.equals(p.getMonthYear()))
                .map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal monthRevenueForBuilding(Long buildingId) {
        String ym = YearMonth.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        return paymentRepo.findAll().stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID
                        && ym.equals(p.getMonthYear())
                        && buildingId.equals(p.getBuildingId()))
                .map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal nonNull(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
