package com.example.demo.config;

import com.example.demo.dto.request.CreateBuildingRequest;
import com.example.demo.dto.request.ReceptionistRequest;
import com.example.demo.dto.request.SharingConfigDto;
import com.example.demo.entity.OwnerProfile;
import com.example.demo.entity.TenantProfile;
import com.example.demo.entity.User;
import com.example.demo.enums.Gender;
import com.example.demo.enums.OwnerStatus;
import com.example.demo.enums.Role;
import com.example.demo.enums.SharingType;
import com.example.demo.repository.OwnerProfileRepository;
import com.example.demo.repository.TenantProfileRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.BuildingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepo;
    private final OwnerProfileRepository ownerRepo;
    private final TenantProfileRepository tenantRepo;
    private final BuildingService buildingService;
    private final PasswordEncoder passwordEncoder;

    @Value("${pg.seed.admin:true}")
    private boolean seedAdmin;

    @Value("${pg.seed.demo:false}")
    private boolean seedDemo;

    public DataSeeder(UserRepository userRepo,
                      OwnerProfileRepository ownerRepo,
                      TenantProfileRepository tenantRepo,
                      BuildingService buildingService,
                      PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.ownerRepo = ownerRepo;
        this.tenantRepo = tenantRepo;
        this.buildingService = buildingService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (seedAdmin) ensureAdmin();
        if (seedDemo) ensureDemo();
    }

    private void ensureAdmin() {
        String email = "admin@pg.local";
        if (userRepo.findByEmailIgnoreCase(email).isPresent()) return;
        User u = new User();
        u.setFullName("System Administrator");
        u.setEmail(email);
        u.setPhone("+10000000000");
        u.setPasswordHash(passwordEncoder.encode("Admin@123"));
        u.setRole(Role.ADMIN);
        u.setEnabled(true);
        userRepo.save(u);
        log.info("Seeded default admin: {} / Admin@123", email);
    }

    private void ensureDemo() {
        String ownerEmail = "owner@pg.local";
        User owner;
        if (userRepo.findByEmailIgnoreCase(ownerEmail).isEmpty()) {
            owner = new User();
            owner.setFullName("Demo Owner");
            owner.setEmail(ownerEmail);
            owner.setPhone("+10000000001");
            owner.setPasswordHash(passwordEncoder.encode("Owner@123"));
            owner.setRole(Role.OWNER);
            owner.setEnabled(true);
            userRepo.save(owner);
            OwnerProfile op = new OwnerProfile();
            op.setUser(owner);
            op.setAddress("123 Demo Street, Pune");
            op.setStatus(OwnerStatus.APPROVED);
            ownerRepo.save(op);
            log.info("Seeded demo owner: {} / Owner@123", ownerEmail);
        } else {
            owner = userRepo.findByEmailIgnoreCase(ownerEmail).get();
        }

        if (userRepo.findByEmailIgnoreCase("reception@pg.local").isEmpty()) {
            CreateBuildingRequest req = new CreateBuildingRequest();
            req.setName("Sunrise PG");
            req.setAddress("Plot 12, MG Road");
            req.setArea("Koregaon Park");
            req.setCity("Pune");
            req.setDescription("Premium PG with all modern amenities, close to IT parks");
            req.setAmenities(List.of("WiFi", "AC", "Laundry", "Hot Water", "Food", "Parking"));
            req.setTotalFloors(3);
            req.setContactPhone("+919999999999");
            req.setContactEmail("info@sunrise.pg");
            req.setImagePaths(new ArrayList<>());

            ReceptionistRequest rr = new ReceptionistRequest();
            rr.setFullName("Demo Receptionist");
            rr.setEmail("reception@pg.local");
            rr.setPhone("+10000000002");
            rr.setPassword("Reception@123");
            req.setReceptionist(rr);

            List<SharingConfigDto> cfgs = new ArrayList<>();
            cfgs.add(cfg(SharingType.ONE, 4, 1, new BigDecimal("15000"), new BigDecimal("15000")));
            cfgs.add(cfg(SharingType.TWO, 6, 2, new BigDecimal("9000"), new BigDecimal("9000")));
            cfgs.add(cfg(SharingType.THREE, 4, 3, new BigDecimal("7000"), new BigDecimal("7000")));
            req.setSharingConfigs(cfgs);

            buildingService.createBuilding(owner.getId(), req);
            log.info("Seeded demo building 'Sunrise PG' with receptionist reception@pg.local / Reception@123");
        }

        for (int i = 1; i <= 3; i++) {
            String em = "tenant" + i + "@pg.local";
            if (userRepo.findByEmailIgnoreCase(em).isPresent()) continue;
            User t = new User();
            t.setFullName("Tenant " + i);
            t.setEmail(em);
            t.setPhone("+1000000010" + i);
            t.setPasswordHash(passwordEncoder.encode("Tenant@123"));
            t.setRole(Role.TENANT);
            t.setEnabled(true);
            userRepo.save(t);
            TenantProfile tp = new TenantProfile();
            tp.setUser(t);
            tp.setGender(i % 2 == 0 ? Gender.FEMALE : Gender.MALE);
            tenantRepo.save(tp);
        }
        log.info("Seeded demo tenants: tenant1..3@pg.local / Tenant@123");
    }

    private SharingConfigDto cfg(SharingType st, int rooms, int beds, BigDecimal rent, BigDecimal deposit) {
        SharingConfigDto d = new SharingConfigDto();
        d.setSharingType(st);
        d.setNumRooms(rooms);
        d.setBedsPerRoom(beds);
        d.setMonthlyRent(rent);
        d.setDepositAmount(deposit);
        return d;
    }
}
