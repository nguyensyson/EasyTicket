# EasyTicket – Cấu trúc Source Code

## 1. Module structure chuẩn

Mỗi Microservice là **Maven Multi-Module Project** gồm 6 module cố định:

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

**Dependency direction:**
```
application → business ← infratructures
application → infratructures
worker      → business
worker      → infratructures
```

- `business` không phụ thuộc `infratructures` – chỉ định nghĩa interface (Port).
- `infratructures` implement interface từ `business` (Adapter pattern).
- `common` không phụ thuộc module nào khác trong cùng service.
- `migration` độc lập, chỉ phụ thuộc Liquibase + JDBC driver.

## 2. Package structure (base package `com.easytickets`)

### application – `com.easytickets.application`
```
Application.java              # @SpringBootApplication, @ComponentScan("com.easytickets.*"), @EnableMethodSecurity
config/                        # SecurityConfig, SecurityProperties, CustomJwtAuthenticationConverter, Redis/Kafka config
controller/{Feature}Controller.java   # @RestController @RequestMapping("api/v1/...")
dto/response|request/          # DTO riêng cho API layer
mapper/                        # application DTO ↔ business DTO
```
Controller chỉ gọi Service interface từ `business`, không đụng Repository/Entity, không chứa business logic.

### business – `com.easytickets.business`
```
config/                        # KeycloakConfig, KeycloakConfigProperties, integration config
dto/{Feature}Request|Dto|Response.java
exception/                     # BusinessException + subclass
repo/{Feature}Repo.java        # Port interface – chỉ nhận/trả DTO, không biết JPA
services/{Feature}Service.java + services/impl/{Feature}ServiceImpl.java
```

### infratructures – `com.easytickets.infratructures`
```
config/JpaConfig.java          # @EnableJpaRepositories, @EntityScan("com.easytickets")
mapper/{Feature}Mapper.java    # MapStruct
model/{Entity}.java + BaseEntity.java
repo/{Feature}Repository.java  # extends JpaRepository
shared/{Feature}RepositoryImpl.java   # implements business.repo.{Feature}Repo (Adapter)
```
Entity không bao giờ expose ra ngoài module này.

### common – `com.easytickets.common`
```
constant/AppConstants.java
dto/ApiResponse.java           # vị trí chuẩn cho response wrapper dùng chung
util/
enums/                         # enum dùng chung, ví dụ RecordStatus
```

### migration – `{ServiceName}-migration`
```
src/main/resources/application.yaml   # datasource + liquibase.enabled=true
src/main/resources/db/changelog/changelog.xml
src/main/resources/db/sources/{V}{n}_{YYYYMMDDHHmm}_{description}.sql
```
Chạy như Spring Boot app riêng, không chứa entity/service/business logic.

### worker – `com.easytickets.worker`
```
consumer/{Topic}Consumer.java   # @KafkaListener
producer/{Topic}Producer.java   # KafkaTemplate wrapper
scheduler/{Job}Scheduler.java   # @Scheduled
```

## 3. Naming convention

| Loại | Pattern | Ví dụ |
|---|---|---|
| Controller | `{Feature}Controller` | `EventController` |
| Service interface / impl | `{Feature}Service` / `{Feature}ServiceImpl` | `TicketService` / `TicketServiceImpl` |
| Repo interface (Port) | `{Feature}Repo` | `OrderRepo` |
| JPA Repository | `{Feature}Repository` | `OrderRepository` |
| Repository Adapter | `{Feature}RepositoryImpl` | `OrderRepositoryImpl` |
| JPA Entity | Danh từ số ít | `Order`, `Event` |
| Request / Internal / Response DTO | `{Feature}Request` / `{Feature}Dto` / `{Feature}Response` | `CreateOrderRequest` |
| MapStruct Mapper | `{Feature}Mapper` | `OrderMapper` |
| Config Properties / Bean | `{Integration}ConfigProperties` / `{Integration}Config` | `KeycloakConfigProperties` |
| Kafka Consumer / Producer | `{Topic}Consumer` / `{Topic}Producer` | `TicketReservedConsumer` |
| Exception | `{Context}Exception` | `TicketSoldOutException` |

- Package: toàn bộ lowercase, không underscore, đặt theo chức năng (`controller`, `service`, `repo`, `model`, `config`, `dto`, `mapper`, `exception`, `consumer`, `producer`).
- SQL migration file: `V{n}_{YYYYMMDDHHmm}_{description}.sql`; DB table `snake_case` số nhiều; DB column `snake_case`; Enum value `SCREAMING_SNAKE_CASE`.

## 4. Luồng xử lý request

```
HTTP Request → [Controller] validate @Valid, gọi Service interface
             → [ServiceImpl] business logic, gọi Port interface (Repo)
             → [RepositoryImpl] Adapter: JpaRepository + Mapper
             → [JpaRepository] → Database
```

Entity không bao giờ ra khỏi `infratructures`. Controller luôn trả `ResponseEntity<ApiResponse<T>>`.

## 5. Checklist tạo service mới

1. Tạo Maven Multi-Module Project với 6 module (`userServiceApplication`, `business`, `common`, `infratructures`, `migration`, `worker`).
2. Parent POM quản lý version trong `<dependencyManagement>`.
3. `common`: `BaseEntity`, `AppConstants`, `ApiResponse`, exception dùng chung.
4. `migration`: `changelog.xml` + SQL script đầu tiên.
5. `business`: DTO, Port interface, Service interface + impl, config.
6. `infratructures`: Entity, JPA Repository, MapStruct Mapper, Adapter, `JpaConfig`.
7. `userServiceApplication`: Controller, `SecurityConfig`, `application.yaml`, `Application.java`.
8. `worker`: Kafka consumer/producer nếu cần.
9. `application.yaml` đủ: datasource, security oauth2, liquibase (tắt ở app, bật ở migration), logging, server port, OTel endpoint.
10. `logback-spring.xml` với `LogstashTcpSocketAppender`.
11. Kiểm tra `@ComponentScan(basePackages = {"com.easytickets.*"})` và `@EnableJpaRepositories(basePackages = {"com.easytickets"})`.

## 6. Trạng thái implement hiện tại (kiểm tra khi bắt đầu việc)

- **UserService**: đã có khung 6-module (pom.xml đầy đủ), nhưng phần lớn còn rỗng – `common` mới có `AppConstants` + `ApiResponse`, `migration` chỉ có `MigrationApplication` trống, `application/business/infratructures/worker` chưa có class nào (chỉ `.gitkeep`). Còn sót thư mục `UserService/src/...` (Application.java + test) ngoài cấu trúc module – tàn dư trước khi tách module, không thuộc kiến trúc chuẩn.
- **EventService, TicketService, OrderService, PaymentService, NotificationService**: vẫn là project Spring Initializr **đơn-module** (chỉ có `{Service}Application.java` + test class), **chưa** tách theo cấu trúc 6-module ở trên. Khi bắt đầu code nghiệp vụ cho các service này, cần tách module trước theo checklist mục 5.
- Chưa có Dockerfile hay Kubernetes manifest nào cho 6 service nghiệp vụ trong repo – `docker-compose.yml` ở root chạy hạ tầng phụ trợ local (MySQL, Redis, Kafka, Keycloak, ELK, OTel Collector) **và** API Gateway giả lập (NGINX + Swagger UI, xem mục dưới), không phải deployment của service.
- **API Gateway (local)**: `infra/nginx/gateway.conf` – container NGINX (`api-gateway` trong `docker-compose.yml`, port `8000`) route request theo path-prefix (`/api/v1/{resource}`) tới từng service đang chạy trên host qua `host.docker.internal`, có `limit_req_zone` giả lập rate-limiting, và proxy `/docs/{service}/` tới `/v3/api-docs` của từng service cho container `swagger-ui` (Swagger UI tổng hợp, truy cập tại `http://localhost:8000`). Đây chỉ là mô phỏng cho local dev – ở dev/prod, vai trò này do **AWS API Gateway** đảm nhiệm, không dùng NGINX/Kubernetes Ingress.
