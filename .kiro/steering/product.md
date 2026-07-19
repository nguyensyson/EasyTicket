# EasyTicket – Product Context

## Mô tả Dự án

EasyTicket là nền tảng phân phối và đặt vé sự kiện trực tuyến theo kiến trúc Microservices, chuyên xử lý Flash Sale với lưu lượng truy cập cực cao. Mục tiêu cốt lõi: **không sập web, không bán vượt số lượng vé (over-selling), phản hồi tính bằng mili-giây**.

## Nguyên tắc Kiến trúc

### Microservices Architecture
- Mỗi service là một ứng dụng Spring Boot độc lập, có database riêng (Database per Service).
- Giao tiếp đồng bộ qua REST API, bất đồng bộ qua Apache Kafka.
- API Gateway: **AWS API Gateway** ở dev/prod để rate-limiting và điều hướng. Ở local, giả lập bằng container NGINX (`docker-compose.yml`) kèm Swagger UI tổng hợp.

### Nguyên tắc Thiết kế
- **Event-Driven**: Các service giao tiếp qua Kafka topics (ticket-reserved, payment-success, payment-failed).
- **Cache-First**: Event Service bọc Redis cache cho tất cả API GET (read-heavy workload).
- **Atomic Inventory**: Tồn kho vé được quản lý hoàn toàn trên Redis bằng Lua Script để đảm bảo tính nguyên tử.
- **Async Order Processing**: Đơn hàng được tạo bất đồng bộ từ Kafka message, không block user request.
- **Distributed Tracing**: Tích hợp OpenTelemetry + Jaeger/Zipkin để truy vết giao dịch xuyên suốt các service.

## Danh sách Services

### 1. API Gateway
- Dev/prod: **AWS API Gateway** – rate-limiting chống bot spam, điều hướng request đến đúng service, TLS termination
- Local: container NGINX trong `docker-compose.yml` giả lập lớp gateway trên, kèm Swagger UI tổng hợp OpenAPI docs (`http://localhost:8000`)

### 2. Event Service (Catalog)
- CRUD sự kiện, số lượng vé, mức giá
- Lên lịch Flash Sale timer
- Cache toàn bộ GET API bằng Redis
- Database: MySQL

### 3. Ticket Service (Core – Flash Sale)
- Nạp tồn kho vé lên Redis khi sự kiện bắt đầu
- Xử lý mua vé bằng Redis Lua Script (CHECK & DECREMENT atomic)
- Publish event `ticket-reserved` lên Kafka
- Lắng nghe `payment-failed` để release vé về Redis

### 4. Order Service
- Consume `ticket-reserved` từ Kafka
- Tạo đơn hàng trạng thái PENDING_PAYMENT
- Cập nhật trạng thái PAID hoặc CANCELLED dựa trên event từ Payment
- Database: MySQL

### 5. Payment Service
- Xử lý thanh toán qua cổng thanh toán
- Timeout: 2 phút cho mỗi giao dịch
- Publish `payment-success` hoặc `payment-failed` lên Kafka

### 6. Notification Service
- Consume event từ Kafka
- Render template vé điện tử (QR Code)
- Gửi email qua AWS SES

## Đối tượng Người dùng

| Role | Mô tả |
|---|---|
| **Organizer** | Nhà tổ chức sự kiện – tạo event, quản lý vé, xem dashboard doanh thu |
| **Ticket Buyer** | Người mua vé – duyệt sự kiện, tham gia Flash Sale, thanh toán, nhận vé |
| **System Admin** | Quản trị viên – duyệt/khóa tài khoản, giám sát hệ thống |

## Tech Stack

- **API Gateway**: AWS API Gateway (dev/prod); NGINX giả lập local (`docker-compose`) + Swagger UI tổng hợp
- **Database**: MySQL (chính), Redis (cache + inventory)
- **Message Broker**: Apache Kafka
- **Email**: AWS SES
- **Monitoring**: OpenTelemetry, Jaeger/Zipkin
- **Container**: Docker, Kubernetes
- **Build Tool**: Maven

## Kafka Topics

| Topic | Producer | Consumer |
|---|---|---|
| `ticket-reserved` | Ticket Service | Order Service |
| `payment-success` | Payment Service | Order Service, Notification Service |
| `payment-failed` | Payment Service | Order Service, Ticket Service |

## Quy tắc Phát triển

### Coding Standards
- Tuân thủ Java coding conventions
- Mỗi service có package structure: `controller`, `service`, `repository`, `model`, `config`, `dto`, `exception`
- Sử dụng DTO để giao tiếp giữa các layer, không expose entity ra ngoài
- Exception handling tập trung qua `@ControllerAdvice`
- API versioning: `/api/v1/...`

### Database
- Mỗi service sở hữu database riêng (Database per Service pattern)
- Sử dụng Liquibase cho database migration
- Naming convention: snake_case cho table và column

### Messaging
- Kafka message format: JSON với schema rõ ràng
- Đảm bảo idempotency khi consume message (xử lý duplicate)
- Dead Letter Queue cho message lỗi

### Testing
- **Không tạo test khi sinh code** – chỉ tạo production code. Test sẽ được thêm thủ công khi cần.
- Nếu được yêu cầu viết test, dùng JUnit 5 + Mockito cho unit test và integration test.

### Deployment
- Mỗi service đóng gói thành Docker image
- Triển khai trên Kubernetes cluster
- Health check endpoint: `/actuator/health`
- Config externalized qua ConfigMap/Secret
