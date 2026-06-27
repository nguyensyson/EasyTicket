# EasyTicket – Cấu trúc Chuẩn cho Microservice

> Tài liệu này được rút ra từ **AuthService** – reference implementation của hệ thống EasyTicket.
> Mọi service mới phải tuân thủ cấu trúc này để đảm bảo tính nhất quán, dễ bảo trì và dễ onboard.

---

## 1. Tổng quan Module Structure

Mỗi Microservice là một **Maven Multi-Module Project** gồm 6 module cố định:

```
{ServiceName}/
├── {ServiceName}-application      # Entry point, Controller, Security Config
├── {ServiceName}-business         # Business logic, Service, DTO, Port (interface repo)
├── {ServiceName}-common           # Shared utilities, constants, base classes
├── {ServiceName}-infratructures   # JPA Entity, Repository impl, Mapper, JpaConfig
├── {ServiceName}-migration        # Liquibase migration scripts (chạy độc lập)
├── {ServiceName}-worker           # Kafka consumers, scheduled jobs, async workers
└── pom.xml                        # Parent POM – quản lý dependency versions
```

**Nguyên tắc phụ thuộc giữa module (Dependency Direction):**
```
application → business ← infratructures
application → infratructures
worker      → business
worker      → infratructures
```
- `business` không phụ thuộc `infratructures`. Business chỉ định nghĩa interface (port).
- `infratructures` implement các interface từ `business` (Adapter pattern).
- `common` không phụ thuộc bất kỳ module nào khác trong cùng service.
- `migration` là module độc lập, chỉ phụ thuộc Liquibase + JDBC driver.

---

## 2. Cấu trúc Package Chi tiết

### 2.1 Base Package

Mọi service sử dụng base package: `com.easytickets`

Convention đặt package theo module:
| Module | Base Package |
|---|---|
| application | `com.easytickets.application` |
| business | `com.easytickets.business` |
| common | `com.easytickets.common` |
| infratructures | `com.easytickets.infratructures` |
| worker | `com.easytickets.worker` |

### 2.2 Application Module – `com.easytickets.application`

```
com.easytickets.application/
├── Application.java                  # @SpringBootApplication, @ComponentScan("com.easytickets.*"), @EnableMethodSecurity
├── config/
│   ├── SecurityConfig.java           # SecurityFilterChain, OAuth2 Resource Server config
│   ├── SecurityProperties.java       # @ConfigurationProperties(prefix = "url") – permit list
│   ├── CustomJwtAuthenticationConverter.java  # Extract roles từ Keycloak JWT claims
│   └── {Other}Config.java            # Redis, Kafka producer config, OpenTelemetry config,...
├── controller/
│   └── {Feature}Controller.java      # @RestController, @RequestMapping("api/v1/...")
├── dto/                              # Request/Response DTO dùng riêng cho API layer
│   ├── response/
│   │   └── ApiResponse.java          # Base response wrapper (xem tech.md)
│   └── request/
└── mapper/                           # Mapper giữa application DTO ↔ business DTO
```

**Quy tắc Application module:**
- `Application.java` luôn đặt ở root package `com.easytickets.application`, dùng `@ComponentScan(basePackages = {"com.easytickets.*"})` để quét toàn bộ module.
- `@EnableMethodSecurity` bật method-level security (`@PreAuthorize`, `@PostAuthorize`).
- Controller chỉ gọi Service interface từ `business` module, không gọi trực tiếp Repository hay Entity.
- Controller không chứa business logic – chỉ validate input, delegate, format response.

### 2.3 Business Module – `com.easytickets.business`

```
com.easytickets.business/
├── config/
│   ├── KeycloakConfig.java           # @Bean Keycloak client
│   ├── KeycloakConfigProperties.java # @ConfigurationProperties(prefix = "keycloak")
│   └── {Integration}Config.java      # Cấu hình tích hợp bên ngoài (Kafka, Redis,...)
├── dto/
│   ├── {Feature}Request.java         # Input DTO từ controller (có @Valid constraints)
│   ├── {Feature}Dto.java             # Internal DTO dùng giữa service ↔ repo
│   └── {Feature}Response.java        # Output DTO trả về cho controller
├── exception/
│   ├── BusinessException.java        # Base business exception
│   ├── ResourceNotFoundException.java
│   └── {Domain}Exception.java        # Domain-specific exceptions
├── mapper/                           # (nếu cần transform giữa business DTOs)
├── repo/
│   └── {Feature}Repo.java            # Interface (Port) – business không biết về JPA
└── services/
    ├── {Feature}Service.java         # Interface – định nghĩa contract
    └── impl/
        └── {Feature}ServiceImpl.java # @Service – implement business logic
```

**Quy tắc Business module:**
- Service interface luôn được định nghĩa, implementation đặt trong `impl/`.
- Repo interface (Port) chỉ nhận và trả DTO, không bao giờ nhận/trả Entity.
- `{Feature}Request.java` – DTO đầu vào từ client, có Bean Validation annotations.
- `{Feature}Dto.java` – Internal transfer object giữa các layer.
- Config beans liên quan đến tích hợp bên ngoài (Keycloak, Redis, Kafka) đặt trong `business/config/`.

### 2.4 Infratructures Module – `com.easytickets.infratructures`

```
com.easytickets.infratructures/
├── config/
│   └── JpaConfig.java                # @EnableJpaRepositories, @EntityScan cho toàn bộ "com.easytickets"
├── mapper/
│   └── {Feature}Mapper.java          # @Mapper(componentModel = "spring") – MapStruct
├── model/
│   ├── {Entity}.java                 # @Entity – JPA Entity
│   ├── {StatusEnum}.java             # Enum dùng trong Entity (@Enumerated(EnumType.STRING))
│   └── BaseEntity.java               # (nên tạo) Abstract base với id, createdAt, updatedAt, deleted
├── repo/
│   └── {Feature}Repository.java      # extends JpaRepository<Entity, ID> – Spring Data JPA
└── shared/
    └── {Feature}RepositoryImpl.java  # implements business.repo.{Feature}Repo – Adapter
```

**Quy tắc Infratructures module:**
- `JpaConfig.java` luôn scan `basePackages = {"com.easytickets"}` để đảm bảo cross-module scan.
- Entity đặt trong `model/`, không bao giờ expose ra ngoài module này.
- `{Feature}RepositoryImpl` trong `shared/` là Adapter: implement interface từ `business/repo/`, dùng `{Feature}Repository` (Spring Data JPA) và `{Feature}Mapper` (MapStruct) để chuyển đổi.
- Mapper trong `infratructures` chuyển đổi giữa business DTO ↔ JPA Entity.

### 2.5 Common Module – `com.easytickets.common`

```
com.easytickets.common/
├── constant/
│   └── AppConstants.java             # Các hằng số dùng chung
├── util/
│   ├── DateTimeUtils.java
│   └── StringUtils.java
└── enums/
    └── {SharedEnum}.java             # Enum dùng chung giữa các service (nếu cần)
```

### 2.6 Migration Module – `{ServiceName}-migration`

```
{ServiceName}-migration/
├── pom.xml                           # Chỉ cần: spring-boot-starter-data-jpa, mysql-connector, liquibase-core
└── src/main/resources/
    ├── application.yaml              # datasource + liquibase config
    └── db/
        ├── changelog/
        │   └── changelog.xml         # Master changelog – include tất cả changeset
        └── sources/
            └── {V}{version}_{timestamp}_{description}.sql  # SQL files
```

**Quy tắc Migration module:**
- Module này chạy độc lập như một Spring Boot app riêng (`spring-boot-starter-data-jpa` + `liquibase-core`).
- Không chứa business logic, entity hay service – chỉ chứa migration scripts.

### 2.7 Worker Module – `com.easytickets.worker`

```
com.easytickets.worker/
├── consumer/
│   └── {Topic}Consumer.java          # @KafkaListener
├── producer/
│   └── {Topic}Producer.java          # KafkaTemplate wrapper
└── scheduler/
    └── {Job}Scheduler.java           # @Scheduled jobs
```

---

## 3. Quy tắc Đặt Tên

### 3.1 Class Naming

| Loại | Pattern | Ví dụ |
|---|---|---|
| Controller | `{Feature}Controller` | `AuthController`, `EventController` |
| Service Interface | `{Feature}Service` | `UsersService`, `TicketService` |
| Service Implementation | `{Feature}ServiceImpl` | `UsersServiceImpl` |
| Repository Interface (Port) | `{Feature}Repo` | `UsersRepo` |
| JPA Repository | `{Feature}Repository` | `UsersRepository` |
| Repository Adapter | `{Feature}RepositoryImpl` | `UserRepositoryImpl` |
| JPA Entity | `{EntityName}` (noun, singular) | `Users`, `Event`, `Order` |
| Request DTO | `{Feature}Request` | `CreateUsersRequest` |
| Internal DTO | `{Feature}Dto` | `CreateUsersDto` |
| Response DTO | `{Feature}Response` | `UserProfileResponse` |
| MapStruct Mapper | `{Feature}Mapper` | `UsersMapper` |
| Config Properties | `{Integration}ConfigProperties` | `KeycloakConfigProperties` |
| Config Bean | `{Integration}Config` | `KeycloakConfig`, `SecurityConfig` |
| Kafka Consumer | `{Topic}Consumer` | `TicketReservedConsumer` |
| Kafka Producer | `{Topic}Producer` | `PaymentEventProducer` |
| Exception | `{Context}Exception` | `UserAlreadyExistsException` |

### 3.2 Package Naming

- Tất cả lowercase, không dùng underscore.
- Theo chức năng: `controller`, `service`, `repo`, `model`, `config`, `dto`, `mapper`, `exception`, `consumer`, `producer`.

### 3.3 File & Resource Naming

| Loại | Pattern | Ví dụ |
|---|---|---|
| SQL Migration | `{V}{number}_{YYYYMMDDHHmm}_{description}.sql` | `V1_202504051652_create_users_table.sql` |
| Liquibase ChangeSet ID | Trùng với tên file SQL (không có `.sql`) | `V1_202504051652_create_users_table` |
| Database table | `snake_case`, số nhiều | `users`, `ticket_orders` |
| Database column | `snake_case` | `keycloak_id`, `created_at` |
| Enum column | `SCREAMING_SNAKE_CASE` | `ACTIVE`, `DELETED` |

---

## 4. Luồng Xử Lý Request

```
HTTP Request
    │
    ▼
[Controller] – validate @Valid, gọi Service interface
    │
    ▼
[ServiceImpl] – business logic, gọi Port interface (Repo)
    │
    ▼
[RepositoryImpl] – Adapter: gọi JpaRepository + Mapper
    │
    ▼
[JpaRepository] – Spring Data JPA → Database
```

**Nguyên tắc:**
- Mỗi layer chỉ biết layer ngay dưới nó thông qua interface.
- Entity không bao giờ truyền ra ngoài `infratructures` module.
- Controller trả về `ResponseEntity<ApiResponse<T>>` – không bao giờ trả Entity trực tiếp.

---

## 5. Checklist Khi Tạo Service Mới

Khi tạo một Microservice mới (ví dụ: `EventService`), làm theo thứ tự:

1. **Tạo Maven Multi-Module Project** với 6 module: `application`, `business`, `common`, `infratructures`, `migration`, `worker`.
2. **Parent POM** quản lý tất cả dependency versions trong `<dependencyManagement>`.
3. **common module** – tạo `BaseEntity`, `AppConstants`, `ApiResponse`, exception classes dùng chung.
4. **migration module** – tạo `changelog.xml` và SQL script đầu tiên.
5. **business module** – tạo DTO, Port interface (Repo), Service interface, ServiceImpl, config.
6. **infratructures module** – tạo Entity, JPA Repository, MapStruct Mapper, Adapter (RepositoryImpl), JpaConfig.
7. **application module** – tạo Controller, SecurityConfig, `application.yaml`, Application.java.
8. **worker module** – tạo Kafka consumers/producers nếu cần.
9. **Cấu hình `application.yaml`** với đầy đủ: datasource, security oauth2, liquibase (tắt ở app, bật ở migration), logging, server port.
10. **Kiểm tra** `@ComponentScan(basePackages = {"com.easytickets.*"})` và `@EnableJpaRepositories(basePackages = {"com.easytickets"})`.

---

## 6. Nguyên tắc Cốt lõi

- **Separation of Concerns**: Mỗi module có trách nhiệm rõ ràng, không overlap.
- **Dependency Inversion**: Business layer định nghĩa interface, Infrastructure implement.
- **DTO Boundary**: Entity chỉ tồn tại trong `infratructures`. DTO là phương tiện giao tiếp giữa các layer.
- **Database per Service**: Mỗi service sở hữu schema riêng, không truy cập database của service khác.
- **Migration Isolation**: Migration module chạy độc lập, không bị ảnh hưởng bởi application startup.
- **Convention over Configuration**: Tuân thủ naming convention để giảm thiểu cấu hình tường minh.
