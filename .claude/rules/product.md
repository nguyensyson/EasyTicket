# EasyTicket – Product & Nghiệp vụ

## Mục tiêu cốt lõi

EasyTicket là nền tảng phân phối và đặt vé sự kiện trực tuyến, kiến trúc Microservices, chuyên xử lý **Flash Sale** lưu lượng cực cao. Ba cam kết bất biến:
1. Không sập web dưới tải cao.
2. Không bao giờ bán vượt số lượng vé (over-selling).
3. Phản hồi tính bằng mili-giây.

## Nguyên tắc kiến trúc

- **Microservices**: mỗi service là Spring Boot app độc lập, **Database per Service** (không service nào truy cập DB của service khác).
- **Giao tiếp**: đồng bộ qua REST (OpenFeign), bất đồng bộ qua Apache Kafka.
- **API Gateway**: NGINX Ingress Controller (Kubernetes) – rate-limiting chống bot, TLS termination, điều hướng request. *(Chưa có manifest/config trong repo – xem [[structure]] về trạng thái hiện tại.)*
- **Event-Driven**: các service giao tiếp nghiệp vụ chính qua Kafka topics.
- **Cache-First**: Event Service bọc Redis cache cho mọi API GET (read-heavy).
- **Atomic Inventory**: tồn kho vé quản lý hoàn toàn trên Redis bằng Lua Script (CHECK & DECREMENT nguyên tử) – đây là cơ chế chống over-selling duy nhất, không dùng lock DB.
- **Async Order Processing**: đơn hàng được tạo từ Kafka message, không block request mua vé.
- **Distributed Tracing**: OpenTelemetry xuyên suốt mọi service (chi tiết ở [[tech-stack]]).

## Danh sách services

| Service | Vai trò | Database |
|---|---|---|
| API Gateway | Rate-limit, chống bot, TLS, điều hướng | – |
| Event Service | CRUD sự kiện/vé/giá, lên lịch flash sale, cache Redis toàn bộ GET | MySQL (`event_db`) |
| Ticket Service | Nạp tồn kho vé lên Redis khi event bắt đầu, xử lý mua vé qua Lua Script, publish `ticket-reserved`, lắng nghe `payment-failed` để release vé | Redis (nguồn sự thật tồn kho) |
| Order Service | Consume `ticket-reserved`, tạo order `PENDING_PAYMENT`, cập nhật `PAID`/`CANCELLED` theo event Payment | MySQL (`order_db`) |
| Payment Service | Xử lý thanh toán, timeout 2 phút/giao dịch, publish `payment-success`/`payment-failed` | MySQL (`payment_db`) |
| Notification Service | Consume event Kafka, render vé QR, gửi email qua AWS SES | MySQL (`notification_db`) |
| User Service | Trung gian với Keycloak, quản lý profile người dùng | MySQL (`user_db`) |

## Kafka topics

| Topic | Producer | Consumer |
|---|---|---|
| `ticket-reserved` | Ticket Service | Order Service |
| `payment-success` | Payment Service | Order Service, Notification Service |
| `payment-failed` | Payment Service | Order Service, Ticket Service |

## Đối tượng người dùng

| Role | Mô tả |
|---|---|
| **Organizer** | Tạo/quản lý event, số lượng vé, mức giá, lên lịch flash sale, xem dashboard doanh thu |
| **Ticket Buyer** | Duyệt/tìm event, tham gia flash sale, thanh toán, nhận vé QR qua email, xem lịch sử |
| **System Admin** | Duyệt/khóa tài khoản organizer, giám sát hệ thống (traffic, CPU/RAM, nghẽn cổ chai) |

## Luồng đặt vé cốt lõi (Flash Sale Flow)

```
User click "Mua vé"
      │
      ▼
  API Gateway ──── Rate Limiting
      │
      ▼
  Ticket Service ──▶ Redis Lua Script: CHECK & DECREMENT
      │
 ┌────┴────┐
Hết vé    Còn vé
 │          │
 ▼          ▼
Response  Kafka(ticket-reserved) → Order Service ghi PENDING_PAYMENT
"Hết vé"                                  │
                                           ▼
                                    Payment Service (timeout 2 phút)
                                     ┌────┴────┐
                                  Success   Failed/Timeout
                                     │          │
                                     ▼          ▼
                                   PAID    CANCELLED + release vé về Redis
                                     │
                                     ▼
                          Notification Service → gửi vé qua email
```

Khi thiết kế tính năng mới liên quan đến flow này, luôn giữ nguyên tắc: **Ticket Service là nguồn sự thật duy nhất cho tồn kho**, mọi thao tác trừ/hoàn vé phải đi qua Lua Script, không được tính toán tồn kho ở service khác.
