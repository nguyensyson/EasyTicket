---
inclusion: manual
---

# EasyTicket – Changelog / Lịch sử Thay đổi

> Mỗi khi có thay đổi đáng kể trong codebase (thêm tính năng, sửa lỗi, refactor, thay đổi cấu hình, cập nhật dependency...), bạn PHẢI ghi lại vào file này theo đúng format bên dưới.
>
> **Quy tắc bắt buộc:**
> - Ghi theo thứ tự **mới nhất lên trên**.
> - Mỗi entry phải có: ngày giờ, loại thay đổi, mô tả, file/module bị ảnh hưởng.
> - Không xóa lịch sử cũ – chỉ thêm vào.

---

## Format Entry

```
### [YYYY-MM-DD HH:mm] – <Loại thay đổi> – <Tiêu đề ngắn>

**Service/Module:** <tên service hoặc module>
**Loại:** FEATURE | BUGFIX | REFACTOR | CONFIG | MIGRATION | DEPENDENCY | SECURITY | DOCS
**Mô tả:**
<Mô tả chi tiết những gì đã thay đổi, lý do thay đổi, tác động>

**Files thay đổi:**
- `path/to/file1.java` – <lý do>
- `path/to/file2.yaml` – <lý do>
```

---

## Lịch sử Thay đổi

### [2026-06-28 12:00] – FEATURE – Tạo Spec (Requirements + Design) cho UserService

**Service/Module:** `UserService`
**Loại:** FEATURE
**Mô tả:**
Tạo toàn bộ tài liệu spec cho UserService – microservice mới trong hệ thống EasyTicket đóng vai trò trung gian với Keycloak và quản lý profile người dùng. Bao gồm tài liệu Requirements (15 yêu cầu EARS-format, phủ toàn bộ login/register/profile/aggregation/security/migration/observability) và tài liệu Design (kiến trúc Maven multi-module 6 module, class design, sequence diagram, DDL MySQL, cấu hình đầy đủ, 12 correctness properties). Database được xác nhận dùng MySQL.

**Files thay đổi:**
- `.kiro/specs/user-service/requirements.md` – Tạo mới (15 requirements, EARS format, MySQL confirmed)
- `.kiro/specs/user-service/design.md` – Tạo mới (high-level design, class design, sequence diagrams, DDL, config, PBT properties)
- `.kiro/specs/user-service/.config.kiro` – Tạo mới (specType: feature, workflowType: requirements-first)

---

### [2026-06-28 00:00] – DOCS – Khởi tạo Changelog Steering File

**Service/Module:** `.kiro/steering`
**Loại:** DOCS
**Mô tả:**
Tạo steering file `changelog.md` để theo dõi lịch sử thay đổi toàn bộ dự án EasyTicket. File này sẽ được cập nhật thủ công (inclusion: manual) mỗi khi có thay đổi đáng kể.

**Files thay đổi:**
- `.kiro/steering/changelog.md` – Tạo mới

---

### [2026-07-19 00:00] – CONFIG – Thay API Gateway sang AWS API Gateway (dev/prod) + NGINX giả lập local kèm Swagger UI

**Service/Module:** `docker-compose.yml`, `infra/nginx`, `EventService`, `TicketService`, `OrderService`, `PaymentService`, `NotificationService`, `UserService`, tài liệu (`Readme.md`, `.claude/rules`, `.kiro/steering`)
**Loại:** CONFIG
**Mô tả:**
Ngừng dùng NGINX Ingress Controller (Kubernetes) làm API Gateway. Ở môi trường dev/prod, API Gateway thật chuyển sang **AWS API Gateway**. Ở local, thêm container NGINX (`api-gateway`) trong `docker-compose.yml` để giả lập lớp gateway đó cho dev: route request theo path-prefix tới từng service (chạy trên host qua `host.docker.internal`), rate limiting cơ bản (`limit_req_zone`), và tích hợp thêm **Swagger UI tổng hợp** (container `swagger-ui`) hiển thị OpenAPI docs của cả 6 service tại `http://localhost:8000`. Để Swagger UI có dữ liệu, đã thêm dependency `springdoc-openapi-starter-webmvc-ui` (v3.0.3) vào module `*-application` của cả 6 service, cấu hình path `/v3/api-docs`, `/swagger-ui.html`, và bổ sung các path này vào `url.permit` để `SecurityConfig` cho phép truy cập công khai. Toàn bộ tài liệu liên quan (README gốc, `infra/README.md`, `.claude/rules/product.md`, `.claude/rules/structure.md`, `.kiro/steering/product.md`) đã cập nhật theo thay đổi này.

**Files thay đổi:**
- `docker-compose.yml` – thêm service `api-gateway` (nginx) và `swagger-ui`
- `infra/nginx/gateway.conf` – tạo mới, cấu hình reverse proxy + rate limit + docs aggregation
- `infra/README.md` – thêm bảng + section hướng dẫn API Gateway/Swagger UI local
- `{EventService,TicketService,OrderService,PaymentService,NotificationService,UserService}/pom.xml` – thêm managed dependency `springdoc-openapi-starter-webmvc-ui`
- `{...}/{...}-application/pom.xml` – thêm dependency springdoc
- `{...}/{...}-application/src/main/resources/application.yaml` – thêm config springdoc + `url.permit` cho `/v3/api-docs/**`, `/swagger-ui.html`, `/swagger-ui/**`
- `Readme.md` – cập nhật kiến trúc, tech stack, bảng service, luồng flash sale, hướng dẫn cài đặt
- `.claude/rules/product.md`, `.claude/rules/structure.md` – cập nhật mô tả API Gateway
- `.kiro/steering/product.md` – cập nhật mô tả API Gateway

---

### [2026-07-19 00:30] – CONFIG – Fix xung đột port giữa Kafka UI và NotificationService

**Service/Module:** `docker-compose.yml`, `infra/README.md`
**Loại:** CONFIG
**Mô tả:**
Kafka UI đang map host port `8085:8080`, trùng với `SERVER_PORT` mặc định (`8085`) của `NotificationService` – nếu chạy `mvnw spring-boot:run` cho NotificationService cùng lúc hạ tầng Docker, hai tiến trình đụng port trên host. Chuyển Kafka UI sang host port `8086` (container port giữ nguyên `8080`), giữ nguyên port của NotificationService vì đã được tham chiếu ở nhiều nơi (gateway, Swagger docs).

**Files thay đổi:**
- `docker-compose.yml` – đổi port mapping Kafka UI từ `8085:8080` sang `8086:8080`
- `infra/README.md` – cập nhật URL Kafka UI từ `localhost:8085` sang `localhost:8086`

---

<!-- Thêm entry mới ở trên dòng này -->
