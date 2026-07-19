# 🎫 EasyTicket – Nền tảng Đặt vé Sự kiện Trực tuyến

## Tổng quan

EasyTicket là nền tảng phân phối và đặt vé sự kiện trực tuyến, được thiết kế chuyên biệt để xử lý các chiến dịch mở bán vé có độ nghẽn mạng cao (Flash Sale). Nền tảng đóng vai trò là cầu nối giữa các nhà tổ chức sự kiện (Organizer) và người hâm mộ (Ticket Buyer).

Với kiến trúc **Microservices** linh hoạt, EasyTicket cam kết mang lại trải nghiệm "săn vé" mượt mà, tốc độ phản hồi tính bằng mili-giây, hoàn toàn loại bỏ tình trạng sập web hay bán vượt quá số lượng vé (over-selling).

> **Trạng thái triển khai hiện tại:** Phần lớn service vẫn ở dạng scaffold (khung module Maven, chưa có business logic/controller). Các endpoint liệt kê trong README này là **thiết kế API mục tiêu** của hệ thống — riêng UserService đã có tài liệu spec chính thức (`.kiro/specs/user-service/`), các service còn lại (Event, Ticket, Order, Payment, Notification) là đề xuất thiết kế theo đúng vai trò nghiệp vụ mô tả ở `.claude/rules/product.md`, chưa được implement thành code.

## Kiến trúc Hệ thống

```
                                  ┌─────────────────┐
                                  │   API Gateway    │
                                  │ (AWS API Gateway │
                                  │  dev/prod; NGINX │
                                  │  giả lập ở local)│
                                  └────────┬─────────┘
                                           │
        ┌───────────────┬─────────────────┼───────────────┬───────────────┐
        ▼               ▼                 ▼               ▼               ▼
 ┌─────────────┐  ┌──────────┐    ┌─────────────┐  ┌──────────┐   ┌──────────────┐
 │Event Service│  │  Ticket  │    │Order Service│  │ Payment  │   │ User Service │
 │  (Catalog)  │  │ Service  │    │             │  │ Service  │   │  (Profile)   │
 └──────┬──────┘  └────┬─────┘    └──────┬──────┘  └────┬─────┘   └──────┬───────┘
        │               │                │              │                │
        ▼               ▼                ▼              ▼                ▼
 ┌─────────────┐  ┌──────────┐    ┌─────────────┐  ┌──────────┐   ┌──────────────┐
 │    MySQL    │  │  Redis   │    │    MySQL    │  │  MySQL   │   │    MySQL     │
 │  + Redis    │  │  + Kafka │    │  + Kafka    │  │ + Kafka  │   │  (user_db)   │
 │  (event_db) │  │(sự thật  │    │ (order_db)  │  │(payment_ │   └──────┬───────┘
 └─────────────┘  │ tồn kho) │    └─────────────┘  │   db)    │          │
                   └──────────┘                    └──────────┘          ▼
                          │                              │        ┌────────────┐
                          ▼                              ▼        │  Keycloak  │
                   ┌────────────────┐              (Kafka: payment│(OAuth2/JWT,│
                   │  Notification  │◄─────────────  -success /   │ Admin API) │
                   │    Service     │                 -failed)    └────────────┘
                   │  (AWS SES)     │
                   └────────────────┘
```

UserService không sở hữu nghiệp vụ xác thực — nó là **lớp trung gian (bridge)** giữa client và Keycloak: nhận username/password rồi delegate sang Keycloak (OpenID Connect Password Grant) để lấy access/refresh token, dùng **Keycloak Admin Client** để tạo user & gán role khi đăng ký, và lưu trữ `user_profiles` (thông tin mở rộng ngoài phạm vi Keycloak) trong database riêng của mình.

## Tech Stack

| Thành phần | Công nghệ |
|---|---|
| Backend | Java 21, Spring Boot 4.1.0, Maven multi-module |
| API Gateway | **AWS API Gateway** (dev/prod); **NGINX** (giả lập local qua `docker-compose`, kèm Swagger UI tổng hợp) |
| Database | MySQL (Database per Service) |
| Cache & Inventory | Redis + Lua Script |
| Message Broker | Apache Kafka |
| Auth | Keycloak (OAuth2/JWT) |
| Email | AWS SES |
| Monitoring | OpenTelemetry Collector → Elasticsearch/Kibana (logs, traces, metrics) |
| Containerization | Docker, Kubernetes |

## Các Microservices

| Service | Vai trò | Database |
|---|---|---|
| **API Gateway** | Rate-limiting, chống bot, TLS termination, điều hướng request. Dev/prod: AWS API Gateway. Local: NGINX container (`docker-compose`) + Swagger UI tổng hợp tại `http://localhost:8000` | – |
| **Event Service** | CRUD sự kiện/loại vé/giá, lên lịch flash sale, cache Redis toàn bộ GET (read-heavy) | MySQL (`event_db`) |
| **Ticket Service** | Nạp tồn kho vé lên Redis khi flash sale bắt đầu, xử lý mua vé bằng Lua Script (CHECK & DECREMENT), publish `ticket-reserved`, lắng nghe `payment-failed` để hoàn vé | Redis (nguồn sự thật tồn kho duy nhất) |
| **Order Service** | Consume `ticket-reserved` để tạo order `PENDING_PAYMENT`, cập nhật `PAID`/`CANCELLED` theo Kafka event từ Payment Service | MySQL (`order_db`) |
| **Payment Service** | Khởi tạo & xử lý giao dịch thanh toán, timeout 2 phút/giao dịch, publish `payment-success`/`payment-failed` | MySQL (`payment_db`) |
| **Notification Service** | Consume Kafka event thanh toán, render vé QR, gửi email qua AWS SES | MySQL (`notification_db`) |
| **User Service** | Cầu nối với Keycloak (login/register/role), quản lý `user_profiles`, tổng hợp lịch sử vé & thống kê tổ chức sự kiện từ Order/Event Service | MySQL (`user_db`) |

### Kafka Topics

| Topic | Producer | Consumer | Nội dung message |
|---|---|---|---|
| `ticket-reserved` | Ticket Service | Order Service | `reservationId`, `userId`, `eventId`, `ticketTypeId`, `quantity`, `unitPrice` |
| `payment-success` | Payment Service | Order Service, Notification Service | `orderId`, `paymentId`, `paidAt` |
| `payment-failed` | Payment Service | Order Service, Ticket Service | `orderId`, `reservationId`, `reason` (`DECLINED` \| `TIMEOUT`) |

Message key = ID entity chính (đảm bảo ordering theo partition). Mọi consumer đều idempotent — kiểm tra `orderId`/`reservationId` đã xử lý chưa trước khi ghi dữ liệu.

## Đối tượng Người dùng

### 1. Nhà tổ chức sự kiện (Organizer)
- Đăng ký, đăng nhập
- Tạo và quản lý sự kiện, số lượng vé, mức giá
- Lên lịch thời gian mở bán (Flash Sale timer)
- Dashboard phân tích: doanh thu, lượng vé bán ra

### 2. Người mua vé (Ticket Buyer)
- Duyệt, tìm kiếm và lọc sự kiện theo danh mục
- Tham gia Flash Sale, tranh vé theo thời gian thực
- Thanh toán trực tuyến, nhận vé QR qua email
- Theo dõi lịch sử giao dịch và trạng thái vé

### 3. Quản trị viên (System Admin)
- Duyệt/khóa tài khoản nhà tổ chức
- Giám sát hệ thống: traffic, CPU/RAM, nghẽn cổ chai
- Theo dõi số lượng event đang publish

---

## API Endpoints

Quy ước chung: mọi endpoint có prefix `api/v1/...`, trả về `ResponseEntity<ApiResponse<T>>` (`{ success, errorCode, message, data, traceId }`), xác thực qua JWT Keycloak (Bearer token) trừ khi ghi rõ "✗" (public). Role kiểm tra qua `@PreAuthorize("hasRole('...')")`.

> **Quy ước module (riêng cho dự án này):** `{Service}-infratructures` không chỉ chứa JPA Entity/Repository — đây là nơi tập trung **toàn bộ giao tiếp với bên thứ 3** (DB qua JPA, Kafka Producer/Consumer, Redis, AWS SES...). `{Service}-worker` chỉ chứa các tác vụ chạy theo lịch/job (`@Scheduled`), không chứa Kafka consumer/producer.

### 1. User Service — `api/v1/users`

*(Đã có spec chính thức tại `.kiro/specs/user-service/`)*

| Method | Path | Auth | Role | Mô tả |
|---|---|---|---|---|
| POST | `/api/v1/users/login` | ✗ | — | Delegate password grant sang Keycloak, trả `accessToken`, `refreshToken`, danh sách role |
| POST | `/api/v1/users/register/buyer` | ✗ | — | Tự đăng ký tài khoản Ticket Buyer |
| POST | `/api/v1/users/register/organizer` | ✗ | — | Tự đăng ký tài khoản Organizer |
| POST | `/api/v1/users/register/admin` | ✓ | `ADMIN` | Admin tạo thêm tài khoản Admin khác |
| GET | `/api/v1/users/me` | ✓ | — | Xem hồ sơ cá nhân của chính mình |
| PUT | `/api/v1/users/me` | ✓ | — | Cập nhật một phần hồ sơ cá nhân (`fullName`, `phone`, `avatarUrl`, `address`) |
| GET | `/api/v1/users/me/ticket-history` | ✓ | — | Lịch sử vé đã mua (Feign gọi Order Service) |
| GET | `/api/v1/users/me/organizer-history` | ✓ | `ORGANIZER` | Thống kê sự kiện đã tổ chức (Feign gọi Event Service) |

Đề xuất bổ sung (chưa có trong spec chính thức, cần cho luồng Admin duyệt/khóa Organizer):

| Method | Path | Auth | Role | Mô tả |
|---|---|---|---|---|
| GET | `/api/v1/users` | ✓ | `ADMIN` | Danh sách tài khoản, lọc theo role/trạng thái |
| PATCH | `/api/v1/users/{userId}/status` | ✓ | `ADMIN` | Khóa/mở khóa tài khoản Organizer (đồng bộ trạng thái `enabled` trên Keycloak qua Admin Client) |

### 2. Event Service — `api/v1/events`

| Method | Path | Auth | Role | Mô tả |
|---|---|---|---|---|
| POST | `/api/v1/events` | ✓ | `ORGANIZER` | Tạo sự kiện mới (draft) |
| PUT | `/api/v1/events/{eventId}` | ✓ | `ORGANIZER` (chủ sở hữu) | Cập nhật thông tin sự kiện |
| DELETE | `/api/v1/events/{eventId}` | ✓ | `ORGANIZER` (chủ sở hữu) | Soft delete sự kiện |
| GET | `/api/v1/events` | ✗ | — | Tìm kiếm/lọc sự kiện theo danh mục, thời gian, thành phố (`locationId`) (cache Redis) |
| GET | `/api/v1/locations` | ✗ | — | Danh sách thành phố/tỉnh dùng để filter sự kiện (cache Redis) |
| GET | `/api/v1/events/{eventId}` | ✗ | — | Chi tiết sự kiện (cache Redis) |
| GET | `/api/v1/events/categories` | ✗ | — | Danh mục sự kiện |
| POST | `/api/v1/events/{eventId}/ticket-types` | ✓ | `ORGANIZER` (chủ sở hữu) | Tạo loại vé + giá + số lượng cho sự kiện |
| PUT | `/api/v1/events/{eventId}/ticket-types/{ticketTypeId}` | ✓ | `ORGANIZER` (chủ sở hữu) | Cập nhật loại vé/giá/số lượng |
| GET | `/api/v1/events/{eventId}/ticket-types` | ✗ | — | Danh sách loại vé của sự kiện (Ticket Service dùng để nạp Redis) |
| POST | `/api/v1/events/{eventId}/flash-sale` | ✓ | `ORGANIZER` (chủ sở hữu) | Lên lịch flash sale (`startAt`, `endAt`) |
| GET | `/api/v1/events/{eventId}/dashboard` | ✓ | `ORGANIZER` (chủ sở hữu) | Dashboard doanh thu, số vé đã bán theo loại vé |
| GET | `/api/v1/events/organizer-history` | ✓ | `ORGANIZER` | *(internal)* Thống kê toàn bộ sự kiện của organizer — được User Service gọi qua Feign |

### 3. Ticket Service — `api/v1/tickets`

Không có DB quan hệ — Redis là nguồn sự thật duy nhất cho tồn kho.

| Method | Path | Auth | Role | Mô tả |
|---|---|---|---|---|
| POST | `/api/v1/tickets/{eventId}/purchase` | ✓ | `BUYER` | Mua vé — thực thi Lua Script CHECK & DECREMENT nguyên tử trên Redis |
| GET | `/api/v1/tickets/{eventId}/availability` | ✗ | — | Xem số vé còn lại theo từng loại vé (real-time) |
| POST | `/api/v1/tickets/{eventId}/load-inventory` | internal | `SYSTEM` | Nạp tồn kho vé từ Event Service vào Redis khi flash sale bắt đầu (kích hoạt bởi `TicketService-worker` scheduler, không public qua Gateway) |

**Infratructures (`TicketService-infratructures`):** giao tiếp với các bên thứ 3 — Kafka Producer `ticket-reserved`; Kafka Consumer `payment-failed` (hoàn vé về Redis); Redis client thực thi Lua Script CHECK & DECREMENT.
**Worker (`TicketService-worker`):** Scheduler đọc lịch flash sale từ Event Service để tự động gọi nạp tồn kho đúng giờ `startAt`.

### 4. Order Service — `api/v1/orders`

| Method | Path | Auth | Role | Mô tả |
|---|---|---|---|---|
| GET | `/api/v1/orders/{orderId}` | ✓ | `BUYER` (chủ sở hữu) / `ADMIN` | Chi tiết đơn hàng và trạng thái |
| GET | `/api/v1/orders/my-tickets` | ✓ | `BUYER` | Lịch sử vé đã mua của chính mình (User Service gọi qua Feign) |
| GET | `/api/v1/orders` | ✓ | `ADMIN` | Danh sách toàn bộ đơn hàng phục vụ giám sát hệ thống |

**Infratructures (`OrderService-infratructures`):** giao tiếp với các bên thứ 3 — Kafka Consumer `ticket-reserved` (tạo order `PENDING_PAYMENT`, idempotent theo `reservationId`); Kafka Consumer `payment-success`/`payment-failed` (cập nhật `PAID`/`CANCELLED`, idempotent theo `orderId`).
**Worker (`OrderService-worker`):** không có tác vụ định kỳ ở giai đoạn hiện tại — module giữ chỗ theo chuẩn cấu trúc, dùng cho job tương lai (ví dụ tự động huỷ order treo quá lâu).

### 5. Payment Service — `api/v1/payments`

| Method | Path | Auth | Role | Mô tả |
|---|---|---|---|---|
| POST | `/api/v1/payments` | ✓ | `BUYER` | Khởi tạo giao dịch thanh toán cho một order (bắt đầu timeout 2 phút) |
| GET | `/api/v1/payments/{paymentId}` | ✓ | `BUYER` (chủ sở hữu) / `ADMIN` | Trạng thái giao dịch |
| POST | `/api/v1/payments/{paymentId}/callback` | webhook (ký số) | — | Callback từ cổng thanh toán bên thứ ba xác nhận kết quả |

**Infratructures (`PaymentService-infratructures`):** giao tiếp với các bên thứ 3 — Kafka Producer `payment-success`/`payment-failed`.
**Worker (`PaymentService-worker`):** Scheduler kiểm tra timeout 2 phút/giao dịch chưa hoàn tất → tự động đánh dấu `FAILED` (gọi Kafka Producer ở `infratructures` để publish `payment-failed`).

### 6. Notification Service — `api/v1/notifications`

| Method | Path | Auth | Role | Mô tả |
|---|---|---|---|---|
| GET | `/api/v1/notifications/me` | ✓ | `BUYER` | Lịch sử thông báo/email đã gửi cho người dùng hiện tại |

**Infratructures (`NotificationService-infratructures`):** giao tiếp với các bên thứ 3 — Kafka Consumer `payment-success` (idempotent theo `orderId` + `type`, publish message ticket-email sang **AWS SQS**, xem Luồng 7 ở trên); Kafka Consumer `payment-failed` (dự kiến, gửi thông báo hủy đơn sang SQS tương tự). Việc gọi AWS SES để thực sự gửi email được tách sang một consumer riêng của queue SQS, chưa nằm trong `NotificationService-infratructures` hiện tại.
**Worker (`NotificationService-worker`):** không có tác vụ định kỳ ở giai đoạn hiện tại — module giữ chỗ, dự kiến dùng cho consumer đọc queue SQS + gọi AWS SES ở giai đoạn sau.

---

## Thiết kế Database

Nguyên tắc chung cho mọi bảng (áp dụng chuẩn `BaseEntity` — xem `.claude/rules/tech-stack.md` mục 6):

- Mọi bảng đều có đủ 6 cột audit: `id CHAR(36) DEFAULT (UUID())` (primary key), `delete_flag ENUM('ACTIVE','DELETED') NOT NULL DEFAULT 'ACTIVE'`, `created_by VARCHAR(255)`, `created_at TIMESTAMP`, `updated_by VARCHAR(255)`, `updated_at TIMESTAMP` — bảng bên dưới **chỉ liệt kê cột nghiệp vụ riêng**, 6 cột audit này mặc định có ở mọi bảng và không lặp lại.
- Xóa dữ liệu = soft delete (`delete_flag = 'DELETED'`), không bao giờ `DROP`/`DELETE` vật lý.
- **Database per Service** — không có foreign key vật lý giữa hai bảng thuộc hai service khác nhau (ví dụ `orders.event_id` không FK sang `event_db.events`), các cột này chỉ là ID tham chiếu logic, được đồng bộ qua Kafka event hoặc gọi REST/Feign khi cần chi tiết.
- Trong cùng một service, các bảng liên quan nhau vẫn dùng FK bình thường (ví dụ `ticket_types.event_id → events.id`).
- Migration đặt tại `{ServiceName}-migration/src/main/resources/db/sources/V{n}_{YYYYMMDDHHmm}_{description}.sql`, chỉ thêm changeSet mới, không sửa/xóa changeSet đã commit.

### 1. User Service — `user_db`

*(Đã triển khai — xem `UserService-migration/.../V1_202506280000_create_user_profiles_table.sql`)*

**Bảng `user_profiles`**

| Cột | Kiểu | Ràng buộc | Ghi chú |
|---|---|---|---|
| `id` | `CHAR(36)` | PK, **không** `@GeneratedValue` | Set thủ công = Keycloak user UUID, không tự sinh |
| `full_name` | `VARCHAR(100)` | `NOT NULL` | |
| `phone` | `VARCHAR(20)` | `NULL` | |
| `avatar_url` | `VARCHAR(255)` | `NULL` | |
| `address` | `VARCHAR(255)` | `NULL` | |

> `username`, `password`, `email`, `role` **không** lưu ở đây — thuộc quyền quản lý của Keycloak. `created_by`/`updated_by` chỉ lưu Keycloak user UUID (JWT claim `sub`).

### 2. Event Service — `event_db`

**Bảng `locations`**

| Cột | Kiểu | Ràng buộc | Ghi chú |
|---|---|---|---|
| `name` | `VARCHAR(100)` | `NOT NULL`, `UNIQUE` | Tên thành phố/tỉnh, dùng để **filter** sự kiện theo khu vực. Ví dụ: `id=1, name="Hà Nội"` |

**Bảng `events`**

| Cột | Kiểu | Ràng buộc | Ghi chú |
|---|---|---|---|
| `organizer_id` | `VARCHAR(255)` | `NOT NULL` | Keycloak user UUID của Organizer sở hữu sự kiện |
| `title` | `VARCHAR(255)` | `NOT NULL` | |
| `description` | `TEXT` | `NULL` | |
| `category` | `VARCHAR(50)` | `NOT NULL` | Ví dụ: `MUSIC`, `SPORTS`, `WORKSHOP` |
| `location_id` | `CHAR(36)` | `NOT NULL`, FK → `locations.id` | Thành phố/tỉnh — dùng để filter (ví dụ `location_id = 1` → "Hà Nội") |
| `location` | `VARCHAR(255)` | `NOT NULL` | Địa chỉ cụ thể của sự kiện. Ví dụ: `"23 Mễ Trì Hạ, Hà Nội"` — chỉ để hiển thị, **không** dùng để filter |
| `banner_url` | `VARCHAR(255)` | `NULL` | |
| `start_time` | `DATETIME` | `NOT NULL` | Thời điểm sự kiện diễn ra |
| `end_time` | `DATETIME` | `NOT NULL` | |
| `status` | `ENUM('DRAFT','PUBLISHED','CANCELLED')` | `NOT NULL DEFAULT 'DRAFT'` | |

> Tách riêng `location_id` (FK → `locations`, ví dụ thành phố "Hà Nội") và `location` (chuỗi địa chỉ chi tiết, ví dụ "23 Mễ Trì Hạ, Hà Nội") để `GET /api/v1/events` có thể filter nhanh theo thành phố (`WHERE location_id = ?` hoặc query cache Redis theo `location_id`) mà không cần parse chuỗi địa chỉ tự do.

**Bảng `ticket_types`**

| Cột | Kiểu | Ràng buộc | Ghi chú |
|---|---|---|---|
| `event_id` | `CHAR(36)` | `NOT NULL`, FK → `events.id` | |
| `name` | `VARCHAR(100)` | `NOT NULL` | Ví dụ: "VIP", "Standard" |
| `price` | `DECIMAL(12,2)` | `NOT NULL` | |
| `total_quantity` | `INT` | `NOT NULL` | Tổng số vé phát hành — **nguồn dữ liệu gốc** để Ticket Service nạp vào Redis lúc flash sale bắt đầu |

> Event Service **không** lưu `sold_quantity` real-time — tồn kho tức thời chỉ tồn tại trên Redis (Ticket Service). Số liệu hiển thị ở dashboard organizer (`GET /api/v1/events/{eventId}/dashboard`) được tổng hợp theo lô (batch) từ dữ liệu Order Service, không dùng để quyết định còn/hết vé.

**Bảng `flash_sales`**

| Cột | Kiểu | Ràng buộc | Ghi chú |
|---|---|---|---|
| `event_id` | `CHAR(36)` | `NOT NULL`, FK → `events.id`, `UNIQUE` | Mỗi event chỉ có một cấu hình flash sale |
| `start_at` | `DATETIME` | `NOT NULL` | |
| `end_at` | `DATETIME` | `NOT NULL` | |
| `status` | `ENUM('SCHEDULED','ACTIVE','ENDED')` | `NOT NULL DEFAULT 'SCHEDULED'` | Cập nhật bởi `EventService-worker` scheduler theo mốc thời gian |

### 3. Ticket Service — không có RDBMS

Theo nguyên tắc kiến trúc (`.claude/rules/product.md`), **Redis là nguồn sự thật duy nhất cho tồn kho vé** — Ticket Service không có bảng SQL nào, do đó `TicketService-migration` không chạy changeSet nghiệp vụ (giữ module rỗng để tuân thủ cấu trúc 6-module chuẩn). Data model thực chất là **Redis key**:

| Redis Key Pattern | Kiểu dữ liệu Redis | Nội dung |
|---|---|---|
| `ticket:inventory:{eventId}:{ticketTypeId}` | `STRING` (số nguyên) | Số vé còn lại — đối tượng thao tác của Lua Script CHECK & DECREMENT / hoàn vé INCREMENT |
| `ticket:reservation:{reservationId}` | `HASH` | `{userId, eventId, ticketTypeId, quantity, unitPrice, reservedAt}` — lưu tạm phục vụ đối chiếu khi consume `payment-failed`; TTL = thời gian timeout thanh toán (2 phút) + buffer |
| `ticket:event:{eventId}:loaded` | `STRING` (cờ boolean) | Đánh dấu đã nạp tồn kho cho event, tránh `TicketService-worker` nạp trùng nếu scheduler chạy lại |

### 4. Order Service — `order_db`

**Bảng `orders`**

| Cột | Kiểu | Ràng buộc | Ghi chú |
|---|---|---|---|
| `reservation_id` | `VARCHAR(255)` | `NOT NULL`, `UNIQUE` | Khóa idempotency — khớp `reservationId` trong Kafka `ticket-reserved`, dùng để consumer kiểm tra đã xử lý chưa |
| `user_id` | `VARCHAR(255)` | `NOT NULL` | Keycloak user UUID của Buyer |
| `event_id` | `CHAR(36)` | `NOT NULL` | Tham chiếu logic sang Event Service (khác DB, không FK) |
| `ticket_type_id` | `CHAR(36)` | `NOT NULL` | Tham chiếu logic sang Event Service |
| `quantity` | `INT` | `NOT NULL` | |
| `unit_price` | `DECIMAL(12,2)` | `NOT NULL` | |
| `total_amount` | `DECIMAL(12,2)` | `NOT NULL` | = `quantity × unit_price` |
| `payment_id` | `CHAR(36)` | `NULL` | Tham chiếu logic sang Payment Service, gán sau khi buyer khởi tạo thanh toán |
| `status` | `ENUM('PENDING_PAYMENT','PAID','CANCELLED')` | `NOT NULL DEFAULT 'PENDING_PAYMENT'` | |

### 5. Payment Service — `payment_db`

**Bảng `payments`**

| Cột | Kiểu | Ràng buộc | Ghi chú |
|---|---|---|---|
| `order_id` | `CHAR(36)` | `NOT NULL`, `UNIQUE` | Tham chiếu logic sang Order Service — 1 order chỉ có tối đa 1 payment còn hiệu lực |
| `user_id` | `VARCHAR(255)` | `NOT NULL` | Keycloak user UUID |
| `amount` | `DECIMAL(12,2)` | `NOT NULL` | |
| `payment_method` | `ENUM('CARD','MOMO','VNPAY','BANK_TRANSFER')` | `NOT NULL` | |
| `status` | `ENUM('PENDING','SUCCESS','FAILED')` | `NOT NULL DEFAULT 'PENDING'` | |
| `external_transaction_id` | `VARCHAR(255)` | `NULL` | Mã giao dịch phía cổng thanh toán thứ 3 |
| `expires_at` | `DATETIME` | `NOT NULL` | = `created_at` + 2 phút — `PaymentService-worker` Scheduler dùng cột này để phát hiện timeout |
| `failed_reason` | `VARCHAR(255)` | `NULL` | `DECLINED` \| `TIMEOUT` |

### 6. Notification Service — `notification_db`

**Bảng `notifications`**

> **Lưu ý kiến trúc (app → SQS → SES):** NotificationService hiện tại **không** gọi AWS SES trực tiếp. Sau khi consume `payment-success`, service chỉ **publish message sang AWS SQS** (queue `ticket-email`); một consumer riêng của queue này (triển khai sau) sẽ đảm nhận việc render QR + gọi AWS SES để thực sự gửi email. Vì vậy bảng `notifications` bên dưới ghi nhận **kết quả publish sang SQS**, không phải kết quả gửi email — cột `user_id`/`recipient` (email người nhận) sẽ được bổ sung bằng migration mới khi phần consume-SQS-gửi-SES được triển khai.

| Cột | Kiểu | Ràng buộc | Ghi chú |
|---|---|---|---|
| `order_id` | `CHAR(36)` | `NOT NULL` | Tham chiếu logic sang Order Service — khóa idempotency khi consume Kafka (kèm `type`) |
| `type` | `ENUM('TICKET_QR','ORDER_CANCELLED')` | `NOT NULL` | |
| `channel` | `ENUM('EMAIL')` | `NOT NULL DEFAULT 'EMAIL'` | Dự phòng mở rộng SMS/Push sau này |
| `status` | `ENUM('QUEUED','QUEUE_FAILED')` | `NOT NULL` | Kết quả publish message sang SQS — **không** phản ánh việc email đã gửi hay chưa |
| `error_message` | `VARCHAR(500)` | `NULL` | Lỗi khi publish sang SQS thất bại |
| `queued_at` | `DATETIME` | `NULL` | Thời điểm publish thành công sang SQS |

> Ràng buộc `UNIQUE (order_id, type)` đảm bảo không publish trùng message cho cùng một order khi Kafka message được deliver lại (idempotency).

---

## Luồng nghiệp vụ chi tiết

### Luồng 1 — Đăng ký tài khoản (Buyer / Organizer / Admin)

**Mô tả:** Người dùng tự đăng ký (Buyer, Organizer) hoặc được Admin tạo tài khoản (Admin). UserService là nơi duy nhất gọi Keycloak Admin Client để tạo user.

**Các bước xử lý:**
1. Client gửi `POST /api/v1/users/register/{buyer|organizer}` (hoặc `/register/admin` kèm JWT của Admin hiện tại) với `username`, `password`, `email`, `fullName`.
2. UserController validate `@Valid RegisterRequest` (username 3–50 ký tự, password 8–128 ký tự, email hợp lệ, fullName 1–100 ký tự). Nếu sai → trả `400 VALIDATION_ERROR` ngay, **không gọi Keycloak**.
3. Với `/register/admin`: Spring Security kiểm tra JWT hợp lệ và role `ADMIN` **trước khi** vào controller — thiếu token → `401`; sai role → `403 FORBIDDEN`.
4. UserServiceImpl gọi Keycloak Admin Client: `realm.users().create(UserRepresentation)`.
   - Nếu username/email đã tồn tại → Keycloak trả `409` → UserService trả `409 USER_ALREADY_EXISTS`, không tạo `UserProfile`.
5. Tạo thành công → Keycloak trả `201` kèm header `Location: .../users/{uuid}` → UserService parse ra `keycloakUserId`.
6. UserService gán role tương ứng (`BUYER`/`ORGANIZER`/`ADMIN`) cho user vừa tạo qua Keycloak Admin Client (`clientLevel(clientId).add(...)`).
7. UserService build `UserProfile` với `id = keycloakUserId`:
   - Đăng ký tự phục vụ (buyer/organizer, không có auth context): `createdBy` **set thủ công** = `keycloakUserId` (vì `AuditorAware` trả rỗng khi không có JWT).
   - Đăng ký admin (có auth context): `createdBy` tự động gán qua JPA Auditing từ JWT `sub` của Admin đang gọi.
8. Lưu `UserProfile` vào `user_db`.
   - Nếu lưu DB thất bại → **rollback**: gọi `keycloak.realm(realm).users().delete(keycloakUserId)` để xoá user vừa tạo trên Keycloak, tránh tài khoản "nửa vời" → trả `500 REGISTRATION_FAILED`.
9. Thành công → trả `201 ApiResponse.ok({ id: keycloakUserId })`, ghi log INFO: `"User registered. userId={}, role={}"` (không log password).

### Luồng 2 — Đăng nhập (Login)

**Mô tả:** UserService không tự xác thực — chỉ chuyển tiếp (delegate) request sang Keycloak.

**Các bước xử lý:**
1. Client gửi `POST /api/v1/users/login` với `username`, `password`.
2. Validate `@Valid LoginRequest` (1–255 ký tự mỗi trường) — sai → `400 VALIDATION_ERROR`, không gọi Keycloak.
3. UserServiceImpl gọi Keycloak token endpoint bằng `RestTemplate`: `POST {serverUrl}/realms/{realm}/protocol/openid-connect/token` với `grant_type=password`, `client_id`, `client_secret`, `username`, `password` (timeout kết nối + đọc = 5s).
4. Keycloak trả `200` kèm `access_token`, `refresh_token` → UserService giải mã JWT, trích role từ claim `resource_access.{clientId}.roles`.
5. Trả `200 ApiResponse.ok({ accessToken, refreshToken, roles })` — **không log** access/refresh token.
6. Nếu Keycloak trả `401` (sai mật khẩu) → UserService trả `401 INVALID_CREDENTIALS`, ghi log WARN (không log password).
7. Nếu Keycloak không phản hồi/timeout/lỗi 5xx → trả `502 KEYCLOAK_UNAVAILABLE`.

### Luồng 3 — Tạo & cấu hình sự kiện, lên lịch Flash Sale (Organizer)

**Mô tả:** Organizer thiết lập toàn bộ thông tin sự kiện trước khi mở bán vé. Đây là bước chuẩn bị dữ liệu cho Luồng 4.

**Các bước xử lý:**
1. Organizer đăng nhập (Luồng 2), nhận JWT có role `ORGANIZER`.
2. `POST /api/v1/events` (Event Service) — tạo sự kiện ở trạng thái draft: tên, mô tả, địa điểm, thời gian diễn ra, danh mục.
3. `POST /api/v1/events/{eventId}/ticket-types` (nhiều lần, mỗi lần một hạng vé) — định nghĩa tên loại vé, giá, số lượng phát hành.
4. `POST /api/v1/events/{eventId}/flash-sale` — cấu hình `startAt`/`endAt` cho đợt mở bán.
5. Event Service ghi vào `event_db` và invalidate/refresh cache Redis tương ứng (mọi API `GET` là cache-first).
6. Tại thời điểm `startAt`, `TicketService-worker` (Scheduler) gọi `GET /api/v1/events/{eventId}/ticket-types` để lấy danh sách loại vé + số lượng, sau đó ghi các key tồn kho vào Redis, ví dụ `ticket:inventory:{eventId}:{ticketTypeId} = quantity`. Từ thời điểm này, **Redis là nguồn sự thật duy nhất** cho tồn kho — Event Service không còn quyết định số vé còn lại.
7. Organizer có thể theo dõi tiến độ bán vé qua `GET /api/v1/events/{eventId}/dashboard`.

### Luồng 4 — Flash Sale: Mua vé (luồng lõi, chống over-selling)

**Mô tả:** Đây là luồng quan trọng nhất của hệ thống — phải đảm bảo không sập web, không bán vượt vé, phản hồi mili-giây.

**Luồng gọi API chi tiết:**
1. Buyer click "Mua vé" trên client → request đi qua **API Gateway** (AWS API Gateway ở dev/prod; NGINX giả lập ở local), bị áp **rate limiting** theo IP/user để chặn bot trước khi chạm tới bất kỳ service nào.
2. Gateway forward tới Ticket Service: `POST /api/v1/tickets/{eventId}/purchase` kèm JWT (role `BUYER`), body `{ ticketTypeId, quantity }`.
3. Ticket Service xác thực JWT (OAuth2 Resource Server), sau đó thực thi **Lua Script** trên Redis cho key `ticket:inventory:{eventId}:{ticketTypeId}`:
   - Script kiểm tra (`CHECK`) tồn kho hiện tại ≥ `quantity` yêu cầu.
   - Nếu đủ, trừ (`DECREMENT`) ngay trong cùng một lệnh Redis — toàn bộ CHECK + DECREMENT là **một thao tác nguyên tử duy nhất**, loại bỏ hoàn toàn race-condition khi hàng nghìn request đến đồng thời (không dùng lock ở tầng DB).
4. **Hết vé** (script trả về không đủ số lượng): Ticket Service trả ngay `409 ApiResponse.error("SOLD_OUT", "Hết vé")`. Không tạo order, không publish Kafka — giữ độ trễ ở mức tối thiểu.
5. **Còn vé** (script trừ kho thành công): Ticket Service publish message vào Kafka topic `ticket-reserved` (key = `eventId` để đảm bảo ordering), payload `{ reservationId, userId, eventId, ticketTypeId, quantity, unitPrice }`, rồi trả **ngay lập tức** `200 ApiResponse.ok({ reservationId })` cho client — **không chờ** Order Service xử lý xong (Async Order Processing, không block request mua vé).
6. `OrderService-infratructures` (Kafka Consumer) nhận `ticket-reserved` (idempotent theo `reservationId` — kiểm tra đã xử lý chưa trước khi ghi) → tạo bản ghi `Order` với `status = PENDING_PAYMENT` trong `order_db`.
7. Client gọi `GET /api/v1/orders/{orderId}` (hoặc nhận qua kênh khác) để xác nhận đơn đã được tạo, rồi tiến hành thanh toán → xem **Luồng 5**.
8. Nếu thanh toán thất bại/hết hạn → **Luồng 6 (hoàn vé)** được kích hoạt, trả tồn kho về Redis cho người khác mua tiếp.

### Luồng 5 — Xử lý thanh toán (timeout 2 phút)

**Mô tả:** Payment Service xử lý giao dịch cho order `PENDING_PAYMENT`, giới hạn 2 phút để tránh giữ chỗ vé vô thời hạn.

**Các bước xử lý:**
1. Client gọi `POST /api/v1/payments` với `{ orderId, paymentMethod }`.
2. Payment Service tạo bản ghi giao dịch `status = PENDING` trong `payment_db`, khởi động đồng hồ timeout 2 phút (`PaymentService-worker` Scheduler).
3. Client hoàn tất thanh toán qua cổng bên thứ ba → cổng thanh toán gọi callback `POST /api/v1/payments/{paymentId}/callback` (webhook có chữ ký) báo kết quả.
4. **Thành công:** Payment Service (`PaymentService-infratructures` Kafka Producer) cập nhật `status = SUCCESS`, publish Kafka `payment-success` `{ orderId, paymentId, paidAt }`.
   - `OrderService-infratructures` (Kafka Consumer) nhận → cập nhật `Order.status = PAID` (idempotent theo `orderId`).
   - `NotificationService-infratructures` (Kafka Consumer) nhận → sang **Luồng 7** (gửi vé qua email).
5. **Thất bại hoặc hết 2 phút chưa thanh toán:** `PaymentService-worker` Scheduler phát hiện timeout (hoặc callback báo lỗi) → Payment Service cập nhật `status = FAILED`, `PaymentService-infratructures` (Kafka Producer) publish `payment-failed` `{ orderId, reservationId, reason }` (`reason` = `DECLINED` hoặc `TIMEOUT`).
   - `OrderService-infratructures` (Kafka Consumer) nhận → cập nhật `Order.status = CANCELLED`.
   - `TicketService-infratructures` (Kafka Consumer) nhận → sang **Luồng 6** (hoàn vé).

### Luồng 6 — Hủy đơn & hoàn vé về Redis

**Mô tả:** Đảm bảo vé không bị "khóa chết" khi buyer không thanh toán — tồn kho phải được trả lại đúng và kịp thời.

**Các bước xử lý:**
1. `TicketService-infratructures` (Kafka Consumer) nhận `payment-failed` (idempotent theo `reservationId` — tránh hoàn vé hai lần nếu message được deliver lại).
2. Thực thi Lua Script **INCREMENT** ngược lại trên key `ticket:inventory:{eventId}:{ticketTypeId}`, cộng trả đúng `quantity` đã trừ ở Luồng 4 bước 3.
3. Từ thời điểm này, vé lại xuất hiện trong kết quả `GET /api/v1/tickets/{eventId}/availability` và có thể được buyer khác mua thành công ở lượt `POST /api/v1/tickets/{eventId}/purchase` tiếp theo.
4. `Order.status` giữ nguyên `CANCELLED` (đã cập nhật ở Luồng 5) làm bằng chứng lịch sử — không xoá vật lý.

### Luồng 7 — Gửi vé điện tử qua email (app → SQS → SES)

**Mô tả:** Sau khi thanh toán thành công, buyer nhận vé có mã QR qua email để check-in tại sự kiện. NotificationService **không gọi AWS SES trực tiếp** — nó chỉ chịu trách nhiệm publish message sang **AWS SQS**; việc render QR và gọi SES để thực sự gửi email được tách thành một consumer riêng của queue này, triển khai ở giai đoạn sau.

**Các bước xử lý (phạm vi hiện tại — app → SQS):**
1. `NotificationService-infratructures` (Kafka Consumer `PaymentSuccessConsumer`) nhận `payment-success` (idempotent theo `orderId` + `type`, kiểm tra qua bảng `notifications` trước khi xử lý — xem `NotificationServiceImpl.processPaymentSuccess`).
2. Build message `{ orderId, paymentId, paidAt, type: "TICKET_QR" }` từ payload Kafka nhận được (chưa gọi thêm Order/Event/User Service).
3. Publish message vào **AWS SQS** (queue `ticket-email`) qua `NotificationQueuePublisher` (port ở `business`, adapter `TicketEmailQueueProducer` ở `infratructures` dùng AWS SDK `SqsClient`). Queue URL cấu hình qua biến môi trường `TICKET_EMAIL_QUEUE_URL` (property `notification.queue.ticket-email-url`).
4. Ghi log kết quả publish (`QUEUED`/`QUEUE_FAILED`) vào `notification_db` (bảng `notifications`) để làm khóa idempotency cho lần retry/deliver-lại của Kafka.
5. Nếu publish sang SQS thất bại, service throw `NotificationQueueFailedException` — Kafka sẽ retry theo cấu hình container mặc định của Spring Boot.

**Các bước xử lý (giai đoạn sau — SQS → SES, chưa triển khai trong repo này):**
6. Một consumer riêng (Lambda hoặc `NotificationService-worker`) đọc queue `ticket-email`, lấy thêm thông tin order/loại vé (Order/Event Service) và email người nhận (Keycloak/UserProfile qua User Service).
7. Render vé điện tử kèm mã QR (mã hoá `orderId`/`ticketId` dùng để check-in), gửi email qua **AWS SES**.
8. Ghi kết quả gửi email (thành công/thất bại) — cần migration bổ sung cột `user_id`/`recipient`/`sent_at` cho bảng `notifications`, có thể tra cứu qua `GET /api/v1/notifications/me`.

### Luồng 8 — Xem lịch sử vé & thống kê tổ chức sự kiện (Aggregation)

**Mô tả:** UserService đóng vai trò tổng hợp (aggregation) dữ liệu từ các service khác cho hồ sơ cá nhân, không tự lưu trữ dữ liệu nghiệp vụ này.

**Luồng gọi API (Buyer xem lịch sử vé):**
1. Client gọi `GET /api/v1/users/me/ticket-history` kèm JWT.
2. UserService xác định `userId` từ JWT `sub`, forward chính JWT đó (header `Authorization`) sang Order Service qua Feign: `GET /api/v1/orders/my-tickets`.
3. Order Service trả danh sách vé đã mua (đọc từ `order_db`) → UserService trả nguyên vẹn `data` này cho client (`200`, danh sách rỗng nếu chưa mua vé nào).
4. Nếu Order Service không phản hồi/lỗi 5xx/timeout (5s) → UserService trả `502 ORDER_SERVICE_UNAVAILABLE`.

**Luồng gọi API (Organizer xem thống kê sự kiện):**
1. Client gọi `GET /api/v1/users/me/organizer-history` kèm JWT role `ORGANIZER` (thiếu role → `403 FORBIDDEN`, không gọi Event Service).
2. UserService forward JWT sang Event Service qua Feign: `GET /api/v1/events/organizer-history`.
3. Event Service trả thống kê (số sự kiện, số vé bán, doanh thu theo từng event của organizer) → UserService trả nguyên vẹn cho client.
4. Lỗi từ Event Service (5xx/timeout) → `502 EVENT_SERVICE_UNAVAILABLE`.

### Luồng 9 — Quản trị & giám sát hệ thống (Admin)

**Mô tả:** System Admin không tham gia vào luồng mua vé, chỉ giám sát và quản lý tài khoản.

**Các bước xử lý:**
1. Admin đăng nhập (Luồng 2) → nhận JWT role `ADMIN`.
2. Xem danh sách tài khoản Organizer: `GET /api/v1/users` (đề xuất bổ sung) → duyệt/khóa qua `PATCH /api/v1/users/{userId}/status`, đồng bộ trạng thái `enabled` trên Keycloak.
3. Tạo thêm tài khoản Admin khác khi cần: `POST /api/v1/users/register/admin` (Luồng 1, bước dành riêng cho Admin).
4. Giám sát traffic, CPU/RAM, độ trễ từng service qua Kibana — dữ liệu đến từ pipeline `Spring Boot → OTel Collector (OTLP) → Elasticsearch → Kibana`, mọi request giữa các service đều mang `traceId`/`spanId` xuyên suốt nên Admin có thể trace được một giao dịch mua vé cụ thể qua toàn bộ 5 service nghiệp vụ.
5. Theo dõi số lượng sự kiện đang publish qua `GET /api/v1/events` (không lọc theo organizer) hoặc dashboard tổng hợp riêng (chưa thiết kế chi tiết).

---

## Cài đặt & Chạy

> _Hướng dẫn đầy đủ (biến môi trường, Dockerfile từng service) sẽ được cập nhật khi các service hoàn thiện — hiện repo chưa có Dockerfile/K8s manifest cho service nghiệp vụ, `docker-compose.yml` chạy hạ tầng phụ trợ local (MySQL, Redis, Kafka, Keycloak, ELK, OTel Collector) và API Gateway giả lập (NGINX + Swagger UI)._

```bash
# Clone repository
git clone <repository-url>
cd EasyTicket

# Khởi động hạ tầng local + API Gateway giả lập + Swagger UI
docker compose up -d

# Build một service (chạy trong thư mục từng service, ví dụ UserService)
cd UserService
./mvnw clean package

# Chạy module application của service (khi đã có business logic)
./mvnw spring-boot:run -pl UserService-application
```

Sau khi cả hạ tầng và service cần dùng đã chạy, gọi API qua **API Gateway giả lập** thay vì gọi thẳng từng service:

- Gateway: `http://localhost:8000` (route theo path `/api/v1/{resource}` tới đúng service, có rate limiting)
- Swagger UI tổng hợp (OpenAPI docs toàn bộ service): `http://localhost:8000/`

Chi tiết cấu hình gateway/swagger: `infra/nginx/gateway.conf`, `infra/README.md`.

## Liên hệ

Nếu bạn có câu hỏi hoặc muốn đóng góp, vui lòng tạo Issue trên repository.
