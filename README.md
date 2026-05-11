# PG Management System

A production-ready PG (Paying Guest) Management System built with **Spring Boot 3.4 (REST API + JWT + Spring Security 6 + Hibernate/JPA)** on **MySQL 8**, and a **pure HTML / CSS / Vanilla-JS** Single Page Application (no React/Vue/Angular, no Bootstrap/Tailwind, no Node).

It supports four roles end-to-end:

- **ADMIN** — system overview, approve/reject PG owners, monitor everything
- **PG OWNER** — register buildings, configure sharing types, create a receptionist per building, monitor occupancy & revenue
- **RECEPTIONIST** — manage one assigned building: booking requests, bed allocation, payments, check-in / check-out
- **TENANT** — browse PGs, filter, send booking requests, view allocation, pay monthly rent, print receipts

---

## Tech Stack

| Layer        | Tech                                                          |
|--------------|---------------------------------------------------------------|
| Backend      | Spring Boot 3.4, Spring Web, Spring Security 6, Spring Data JPA, Hibernate 6, Bean Validation |
| Auth         | JWT (jjwt 0.12.6, HS256) + BCrypt                              |
| Database     | MySQL 8 (auto-creates schema via Hibernate `ddl-auto=update`) |
| Frontend     | Pure HTML5 + CSS3 + Vanilla JavaScript (ES2017+), hash router, hand-rolled SVG charts |
| Uploads      | Local filesystem (`./uploads/`) served via Spring static handler |
| Build        | Maven 3 + Maven Wrapper (`mvnw`)                                |

No third-party JS dependencies — everything (charts, routing, modal, toast, table, forms, polling) is hand-built.

---

## Prerequisites

- **Java 17+** (`java -version`)
- **MySQL 8** running locally on port `3306` (or update `application.properties`)
- **Maven 3.8+** — optional; you can use the included wrapper (`mvnw` / `mvnw.cmd`)

## Quick Start

```bash
# 1. Make sure MySQL is up. The default datasource uses root / root.
#    The app will create the database `pg_management` automatically.

# 2. Run
./mvnw spring-boot:run          # macOS / Linux
mvnw.cmd spring-boot:run        # Windows PowerShell

# 3. Open
#    http://localhost:8085/
```

On first start, the seeder creates a default admin:

```
Email:    admin@pg.local
Password: Admin@123
```

To also seed a demo PG (1 owner, 1 receptionist, 1 building with 14 rooms & 30 beds, 3 tenants), set `pg.seed.demo=true` in `application.properties` before starting:

```properties
pg.seed.demo=true
```

Demo accounts:

| Role         | Email                  | Password        |
|--------------|------------------------|-----------------|
| Admin        | admin@pg.local         | `Admin@123`     |
| Owner        | owner@pg.local         | `Owner@123`     |
| Receptionist | reception@pg.local     | `Reception@123` |
| Tenant 1..3  | tenant1@pg.local..     | `Tenant@123`    |

## Configuration

`src/main/resources/application.properties`

```properties
server.port=8085
spring.datasource.url=jdbc:mysql://localhost:3306/pg_management?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root

app.uploads.dir=./uploads
app.jwt.secret=<base64-256-bit-secret>
app.jwt.expiration-ms=86400000
app.cors.allowed-origins=http://localhost:8085

pg.seed.admin=true
pg.seed.demo=false
```

> **Production note:** rotate `app.jwt.secret` to a freshly generated 32+ byte secret. The default is for local development only.

---

## End-to-end Flow

1. **Owner** registers → status `PENDING`, cannot login yet.
2. **Admin** approves owner → owner is enabled.
3. **Owner** logs in → creates a building (multi-step form):
   * basic info, amenities, images
   * receptionist account (auto-creates a `RECEPTIONIST` user uniquely linked to this building)
   * 1/2/3 sharing configs → auto-generates rooms and beds
4. **Tenant** registers → can login immediately.
5. **Tenant** browses PGs → filters by city / area / sharing / rent / availability.
6. **Tenant** picks a sharing type and sends a booking request.
7. **Receptionist** sees the request → approves and **allocates** an `AVAILABLE` bed:
   * Atomic `@Transactional` + `PESSIMISTIC_WRITE` lock + JPA `@Version` ensure **no double-allocation**.
   * Bed status flips `AVAILABLE` → `OCCUPIED`, tenant pointer is set, request status becomes `ALLOCATED`.
   * The first monthly `Payment` row is created automatically (status `UNPAID`).
8. **Receptionist** collects rent → marks `PAID` + generates a receipt (`RCPT-YYYYMM-<id>`).
9. **Tenant** can also pay online (demo) → also gets a receipt.
10. **Receptionist** checks tenant out → bed flips back to `AVAILABLE`.

All dashboards (Admin / Owner / Receptionist / Tenant) **poll every 8–12 s** for live updates of beds, requests, payments and analytics.

---

## Architecture

```
src/main/java/com/example/demo/
├── PgApplication.java
├── config/        SecurityConfig, JwtProperties, AppProperties, WebConfig, DataSeeder
├── security/      JwtUtil, JwtAuthFilter, AuthEntryPoint, AccessDeniedHandlerImpl,
│                  CustomUserDetailsService, AuthUser, CurrentUser
├── entity/        User, OwnerProfile, TenantProfile, Building, BuildingImage,
│                  SharingConfig, Room, Bed, BookingRequest, Payment
├── enums/         Role, OwnerStatus, SharingType, BedStatus, BookingStatus,
│                  PaymentStatus, Gender
├── repository/    one JpaRepository per entity
├── dto/
│   ├── request/   Login, Register*, CreateBuilding, AddRoom, UpdateRoom,
│   │              UpdateBuilding, SharingConfigDto, BookingRequestDto,
│   │              AllocateBedRequest, CollectPaymentRequest, RejectRequest,
│   │              ReceptionistRequest
│   └── response/  AuthResponse, UserDto, BuildingDto, SharingConfigResponseDto,
│                  RoomDto, BedDto, AvailabilityDto, BookingRequestResponseDto,
│                  PaymentDto, ReceiptDto, DashboardDto, PageResponse, ApiMessage
├── service/       AuthService, AdminService, BuildingService, BuildingMapper,
│                  ReceptionistService, BookingService, AllocationService,
│                  PaymentService, BuildingBrowseService, AnalyticsService,
│                  FileStorageService
├── controller/    AuthController, AdminController, OwnerController,
│                  ReceptionistController, TenantController,
│                  BuildingPublicController, AvailabilityController,
│                  ReceiptController, FileController
└── exception/     ApiException, ResourceNotFoundException, BadRequestException,
                   ConflictException, ForbiddenException, GlobalExceptionHandler

src/main/resources/
├── application.properties
└── static/
    ├── index.html
    ├── css/main.css
    └── js/
        ├── util.js, api.js, auth.js, ui.js, charts.js, router.js
        └── pages/
            ├── login.js, register.js, shared.js
            └── admin.js, owner.js, receptionist.js, tenant.js
```

### Data Model

```
PGOwner (User+OwnerProfile) ──1:N── Building ──1:N── Room ──1:N── Bed
                                          │
                                          1:1 Receptionist (User)
                                          1:N SharingConfig (ONE/TWO/THREE)
                                          1:N BuildingImage
                                          1:N BookingRequest ──N:1── Tenant (User+TenantProfile)
                                          1:N Payment        ──N:1── Tenant
```

Key invariants:
- `Building.receptionistUserId` is **unique** → enforces one-receptionist-per-building.
- `SharingConfig (buildingId, sharingType)` is **unique** → at most one config per sharing type per building.
- `Payment (tenantUserId, monthYear)` is **unique** → no duplicate monthly bill.
- `Bed` has `@Version` and is locked with `PESSIMISTIC_WRITE` during allocation/checkout.

### Security

- Stateless JWT (HS256), `Authorization: Bearer <token>`.
- BCrypt password hashing.
- Role-based URL guards:
  - `/api/admin/**` → `ADMIN`
  - `/api/owner/**` → `OWNER` (only after `OwnerStatus=APPROVED` enables the user)
  - `/api/receptionist/**` → `RECEPTIONIST`
  - `/api/tenant/**` → `TENANT`
  - `/api/auth/**`, `/api/public/**`, static files, `/uploads/**` → public
- Spring `@PreAuthorize("isAuthenticated()")` on receipt and availability endpoints.

---

## REST API

| Method | Path                                                | Role            | Purpose                                         |
|--------|-----------------------------------------------------|-----------------|-------------------------------------------------|
| POST   | `/api/auth/register/tenant`                         | public          | Register tenant                                 |
| POST   | `/api/auth/register/owner`                          | public          | Register owner (status=PENDING)                 |
| POST   | `/api/auth/login`                                   | public          | Returns `{ token, user }`                       |
| GET    | `/api/auth/me`                                      | any auth        | Current user                                    |
| POST   | `/api/public/files/upload`                          | public          | Upload ID-proof (registration)                  |
| POST   | `/api/files/upload`                                 | any auth        | Upload building images / generic                |
| GET    | `/api/admin/dashboard`                              | ADMIN           | Admin KPIs & chart data                         |
| GET    | `/api/admin/owners?status=&page=&size=`             | ADMIN           | Paginated owners                                |
| POST   | `/api/admin/owners/{id}/approve`                    | ADMIN           | Approve owner                                   |
| POST   | `/api/admin/owners/{id}/reject`                     | ADMIN           | Reject owner (body: `{ reason }`)               |
| GET    | `/api/admin/buildings?q=&page=&size=`               | ADMIN           | All buildings                                   |
| GET    | `/api/admin/tenants?q=&page=&size=`                 | ADMIN           | All tenants                                     |
| GET    | `/api/admin/receptionists?q=&page=&size=`           | ADMIN           | All receptionists                               |
| GET    | `/api/owner/dashboard`                              | OWNER           | Owner KPIs (per-building)                       |
| GET    | `/api/owner/buildings`                              | OWNER           | List my buildings                               |
| POST   | `/api/owner/buildings`                              | OWNER           | Create building + sharing + rooms + receptionist|
| GET    | `/api/owner/buildings/{id}`                         | OWNER           | Building detail                                 |
| PUT    | `/api/owner/buildings/{id}`                         | OWNER           | Update building (no rooms changed)              |
| DELETE | `/api/owner/buildings/{id}`                         | OWNER           | Delete building (blocked if occupied beds)      |
| GET    | `/api/owner/buildings/{id}/rooms`                   | OWNER           | Rooms with occupancy                            |
| POST   | `/api/owner/buildings/{id}/rooms`                   | OWNER           | Add a single room                               |
| PUT    | `/api/owner/rooms/{roomId}`                         | OWNER           | Update room rent / deposit / floor              |
| DELETE | `/api/owner/rooms/{roomId}`                         | OWNER           | Delete room (blocked if occupied)               |
| GET    | `/api/receptionist/dashboard`                       | RECEPTIONIST    | Building KPIs                                   |
| GET    | `/api/receptionist/building`                        | RECEPTIONIST    | My assigned building                            |
| GET    | `/api/receptionist/rooms`                           | RECEPTIONIST    | Rooms with bed-level detail                     |
| GET    | `/api/receptionist/tenants`                         | RECEPTIONIST    | Active tenants                                  |
| GET    | `/api/receptionist/requests?status=&page=&size=`    | RECEPTIONIST    | Booking requests                                |
| POST   | `/api/receptionist/requests/{id}/approve`           | RECEPTIONIST    | Approve booking request                         |
| POST   | `/api/receptionist/requests/{id}/reject`            | RECEPTIONIST    | Reject (body: `{ reason }`)                     |
| GET    | `/api/receptionist/available-beds?buildingId=&sharingType=` | RECEPTIONIST | Beds the user can allocate |
| POST   | `/api/receptionist/allocate`                        | RECEPTIONIST    | `{ bookingRequestId, bedId }` — atomic         |
| POST   | `/api/receptionist/checkout/{tenantId}`             | RECEPTIONIST    | Free the bed                                    |
| GET    | `/api/receptionist/payments?status=&page=&size=`    | RECEPTIONIST    | Payments list                                   |
| POST   | `/api/receptionist/payments/collect`                | RECEPTIONIST    | Mark paid, returns receipt number               |
| POST   | `/api/receptionist/payments/generate?monthYear=`    | RECEPTIONIST    | Generate UNPAID rows for current tenants        |
| GET    | `/api/tenant/dashboard`                             | TENANT          | My allocation summary                           |
| GET    | `/api/public/buildings?q=&city=&area=&sharingType=&maxRent=&availableOnly=&page=&size=` | public | Browse |
| GET    | `/api/public/buildings/{id}`                        | public          | Building detail (live availability)             |
| GET    | `/api/public/buildings/{id}/availability`           | public          | Live availability                               |
| POST   | `/api/tenant/bookings`                              | TENANT          | Submit booking request                          |
| GET    | `/api/tenant/bookings?page=&size=`                  | TENANT          | My requests                                     |
| POST   | `/api/tenant/bookings/{id}/cancel`                  | TENANT          | Cancel pending/approved request                 |
| GET    | `/api/tenant/payments`                              | TENANT          | My payments                                     |
| POST   | `/api/tenant/payments/{id}/pay`                     | TENANT          | Pay online (demo, returns receipt)              |
| GET    | `/api/availability/buildings/{id}`                  | any auth        | Polled by dashboards                            |
| GET    | `/api/receipts/{receiptNumber}`                     | any auth        | Receipt details                                 |

Errors are returned as:
```json
{
  "timestamp": "2026-05-11T10:30:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Bed is not available",
  "path": "/api/receptionist/allocate"
}
```

---

## Frontend

- `index.html` is a single shell. `js/router.js` is a tiny hash router with role guards.
- All views are built with vanilla `document.createElement` and the small `PG.el(...)` helper in `js/util.js`.
- `js/api.js` is a fetch wrapper that automatically injects the JWT and redirects to `#/login` on 401.
- `js/charts.js` renders SVG donut, bar, and mini-line charts — no Chart.js / D3.
- Dashboards poll their data endpoint every 8–12 seconds for live updates.
- Print receipts are rendered inline with print-only CSS — your browser's native "Print to PDF" handles export.

---

## Notes & Limitations

- Spring's `ddl-auto=update` auto-creates / evolves tables on first boot. For production, generate Flyway/Liquibase migrations.
- Payment processing is **simulated** — there is no integration with a real gateway. The "Pay" buttons flip statuses and generate a receipt number.
- Image storage is local disk; for production, switch `FileStorageService` to S3/equivalent.
- Polling refreshes everything from authoritative sources, so the "live" updates are eventually consistent within the polling interval (8–12 s).

---

## License

MIT (or your preference).
