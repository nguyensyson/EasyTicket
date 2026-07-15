# 🎫 EasyTicket – Nền tảng Đặt vé Sự kiện Trực tuyến

## Tổng quan

EasyTicket là nền tảng phân phối và đặt vé sự kiện trực tuyến, được thiết kế chuyên biệt để xử lý các chiến dịch mở bán vé có độ nghẽn mạng cao (Flash Sale). Nền tảng đóng vai trò là cầu nối giữa các nhà tổ chức sự kiện (Organizer) và người hâm mộ (Ticket Buyer).

Với kiến trúc **Microservices** linh hoạt, EasyTicket cam kết mang lại trải nghiệm "săn vé" mượt mà, tốc độ phản hồi tính bằng mili-giây, hoàn toàn loại bỏ tình trạng sập web hay bán vượt quá số lượng vé (over-selling).

## Kiến trúc Hệ thống

```
                        ┌─────────────────┐
                        │   NGINX Ingress  │
                        │   (API Gateway)  │
                        └────────┬─────────┘
                                 │
          ┌──────────────┬───────┴───────┬──────────────┐
          ▼              ▼               ▼              ▼
   ┌─────────────┐ ┌──────────┐ ┌─────────────┐ ┌──────────┐
   │Event Service│ │  Ticket  │ │Order Service│ │ Payment  │
   │  (Catalog)  │ │ Service  │ │             │ │ Service  │
   └──────┬──────┘ └────┬─────┘ └──────┬──────┘ └────┬─────┘
          │              │              │              │
          ▼              ▼              ▼              ▼
   ┌─────────────┐ ┌──────────┐ ┌─────────────┐ ┌──────────┐
   │    MySQL    │ │  Redis   │ │    MySQL    │ │   Kafka  │
   │  + Redis    │ │  + Kafka │ │  + Kafka    │ │          │
   └─────────────┘ └──────────┘ └─────────────┘ └──────────┘
                                                       │
                                                       ▼
                                              ┌────────────────┐
                                              │  Notification  │
                                              │    Service     │
                                              │  (AWS SES)     │
                                              └────────────────┘
```

## Tech Stack

| Thành phần | Công nghệ |
|---|---|
| Backend | Java, Spring Boot |
| API Gateway | NGINX Ingress Controller |
| Database | MySQL |
| Cache & Inventory | Redis + Lua Script |
| Message Broker | Apache Kafka |
| Email | AWS SES |
| Monitoring | OpenTelemetry + Jaeger/Zipkin |
| Containerization | Docker, Kubernetes |

## Các Microservices

| Service | Vai trò |
|---|---|
| **API Gateway** | Rate-limiting, chống bot, điều hướng request |
| **Event Service** | Quản lý thông tin sự kiện (Read-heavy), cache với Redis |
| **Ticket Service** | Xử lý tồn kho vé bằng Redis Lua Script (Atomic operations) |
| **Order Service** | Quản lý vòng đời đơn hàng (Async qua Kafka) |
| **Payment Service** | Xử lý thanh toán, phát event kết quả |
| **Notification Service** | Gửi vé điện tử qua email (AWS SES) |

## Đối tượng Người dùng

### 1. Nhà tổ chức sự kiện (Organizer)
- Đăng ký, Đăng nhâp
- Tạo và quản lý sự kiện, số lượng vé, mức giá
- Lên lịch thời gian mở bán (Flash Sale timer)
- Dashboard phân tích: doanh thu, lượng vé bán ra

### 2. Người mua vé (Ticket Buyer)
- Duyệt, tìm kiếm và lọc sự kiện theo danh mục
- Tham gia Flash Sale, tranh vé theo thời gian thực
- Thanh toán trực tuyến, nhận vé QR qua email
- Theo dõi lịch sử giao dịch và trạng thái vé

### 3. Quản trị viên (System Admin)
- Quan ly tài khoản nhà tổ chức
- Giám sát hệ thống: traffic, CPU/RAM, nghẽn cổ chai
- Theo dõi số lượng event đang publish

## Luồng Đặt vé Cốt lõi (Flash Sale Flow)

```
User click "Mua vé"
       │
       ▼
┌─────────────────┐
│  API Gateway    │──── Rate Limiting
└────────┬────────┘
         │
         ▼
┌─────────────────┐     ┌───────┐
│ Ticket Service  │────▶│ Redis │  Lua Script: CHECK & DECREMENT
└────────┬────────┘     └───────┘
         │
    ┌────┴────┐
    │         │
  Hết vé   Còn vé
    │         │
    ▼         ▼
 Response   Kafka (ticket-reserved)
 "Hết vé"    │
              ▼
       ┌─────────────┐
       │Order Service │  Ghi DB: PENDING_PAYMENT
       └──────┬──────┘
              │
              ▼
       ┌─────────────┐
       │  Payment    │  Thanh toán (timeout: 2 phút)
       └──────┬──────┘
         ┌────┴────┐
         │         │
      Success    Failed/Timeout
         │         │
         ▼         ▼
      PAID      CANCELLED + Release vé về Redis
         │
         ▼
   Notification Service → Gửi vé qua email
```

## Cài đặt & Chạy

> _Hướng dẫn chi tiết sẽ được cập nhật khi các service được triển khai._

```bash
# Clone repository
git clone <repository-url>
cd EasyTicket

# Khởi động infrastructure
docker-compose up -d

# Build tất cả services
./mvnw clean package
```

## Liên hệ

Nếu bạn có câu hỏi hoặc muốn đóng góp, vui lòng tạo Issue trên repository.
