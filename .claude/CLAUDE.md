# EasyTicket

Nền tảng đặt vé sự kiện trực tuyến, kiến trúc Microservices, chuyên xử lý **Flash Sale** lưu lượng cực cao. Ba cam kết cốt lõi: không sập web, không over-selling vé, phản hồi tính bằng mili-giây.

6 service nghiệp vụ: **Event, Ticket, Order, Payment, Notification, User** — mỗi service có database riêng (Database per Service), giao tiếp REST (đồng bộ) + Kafka (bất đồng bộ). Tồn kho vé quản lý nguyên tử trên Redis bằng Lua Script (CHECK & DECREMENT) tại Ticket Service — đây là điểm chống over-selling duy nhất.

3 role người dùng: **Organizer** (tạo/quản lý event), **Ticket Buyer** (mua vé), **System Admin** (giám sát hệ thống).

Chi tiết nghiệp vụ đầy đủ: @.claude/rules/product.md

## Trạng thái hiện tại — đọc trước khi code

Phần lớn service còn ở dạng **scaffold**, chưa có business logic:
- **UserService** đã có khung 6-module Maven nhưng gần như rỗng.
- **EventService, TicketService, OrderService, PaymentService, NotificationService** vẫn là project đơn-module (chỉ có `{Service}Application.java`), **chưa tách theo cấu trúc 6-module chuẩn**.
- Chưa có Dockerfile / Kubernetes manifest nào trong repo.

→ Trước khi thêm tính năng cho một service đơn-module, phải tách cấu trúc 6-module trước (xem checklist ở @.claude/rules/structure.md). Không giả định class/file đã tồn tại — luôn kiểm tra thực tế trước khi sửa.

## Tech stack & lệnh chính

- **Backend**: Java 21, Spring Boot `4.1.0`, Maven multi-module.
- **Database**: MySQL (mỗi service 1 schema riêng), Redis (cache + tồn kho vé).
- **Message broker**: Apache Kafka. **Auth**: Keycloak (OAuth2/JWT). **Email**: AWS SES.
- **Observability**: OpenTelemetry Collector → Elasticsearch/Kibana (logs, traces qua APM, metrics).

```bash
docker compose up -d          # khởi động hạ tầng local (MySQL, Redis, Kafka, Keycloak, ELK, OTel, API Gateway NGINX + Swagger UI)
./mvnw clean package           # build service (chạy trong thư mục từng service)
./mvnw spring-boot:run          # chạy 1 service (module application, nếu đã tách module)
```

Chi tiết version, logging, exception handling, Liquibase, security, Kafka, API convention: @.claude/rules/tech-stack.md

## Cấu trúc thư mục

```
{ServiceName}/
├── {ServiceName}-application      # Controller, SecurityConfig, entry point
├── {ServiceName}-business         # Service, DTO, Port interface (repo)
├── {ServiceName}-common           # BaseEntity, ApiResponse, constants dùng chung
├── {ServiceName}-infratructures   # JPA Entity, Repository impl, Mapper
├── {ServiceName}-migration        # Liquibase, chạy độc lập với application
└── {ServiceName}-worker           # Kafka consumer/producer, scheduled job
```

Dependency direction: `application → business ← infratructures`; `business` không phụ thuộc `infratructures` (chỉ định nghĩa Port interface); `infratructures` implement Port (Adapter pattern); Entity **không bao giờ** rời khỏi `infratructures`.

Naming convention, package layout chi tiết từng module, checklist tạo service mới: @.claude/rules/structure.md

## Luôn phải làm

- Entity extend `BaseEntity`, có đủ audit columns (`delete_flag`, `created_by/at`, `updated_by/at`); audit field gán tự động qua JPA Auditing, **không set thủ công**.
- Xóa dữ liệu = soft delete (`delete_flag = DELETED`), **không bao giờ xóa vật lý**.
- Trả API qua `ResponseEntity<ApiResponse<T>>` (`com.easytickets.common.dto.ApiResponse`), không trả Entity/kiểu thô trực tiếp.
- Dùng `BusinessException`/subclass cho lỗi nghiệp vụ; xử lý tập trung qua `@RestControllerAdvice`.
- Log bằng SLF4J (`@Slf4j`), kèm context (`userId`, `orderId`...); mọi endpoint có prefix `api/v1/...`.
- Consumer Kafka phải idempotent (check `orderId`/`eventId` đã xử lý chưa) trước khi xử lý message.
- Secrets/credentials luôn qua `${ENV_VAR:default}`, không hardcode.
- Migration: chỉ thêm changeSet mới, không sửa changeSet đã commit, không `DROP TABLE`.

## Không bao giờ được làm

- Không throw `RuntimeException` trần — dùng `BusinessException`.
- Không log password, token, số thẻ, PII đầy đủ.
- Không để Entity/JPA rò ra khỏi module `infratructures`.
- Không hardcode danh sách public endpoint trong `SecurityConfig` — cấu hình qua `url.permit` trong `application.yaml`.
- **Không tự sinh test** (`*Test.java`, file trong `src/test/`, mock/stub) khi tạo/sửa code — chỉ production code, trừ khi được yêu cầu cụ thể.
