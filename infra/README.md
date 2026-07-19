# EasyTicket – Local Infrastructure

## Tổng quan

| Service | Image | Port | URL / Connection |
|---|---|---|---|
| **MySQL** | `mysql:8.0` | `3306` | `jdbc:mysql://localhost:3306/{db}` |
| **Redis** | `redis:7-alpine` | `6379` | `redis://localhost:6379` |
| **Zookeeper** | `wurstmeister/zookeeper:3.4.6` | `2181` | `localhost:2181` |
| **Kafka** | `wurstmeister/kafka:2.13-2.8.1` | `9092` | `localhost:9092` |
| **Kafka UI** | `provectuslabs/kafka-ui:latest` | `8086` | http://localhost:8086 |
| **Keycloak** | `quay.io/keycloak/keycloak:26.0.4` | `8080` | http://localhost:8080 |
| **Elasticsearch** | `elasticsearch:8.10.2` | `9200` | http://localhost:9200 |
| **Logstash** | `logstash:8.10.0` | `5000` (TCP) | `localhost:5000` |
| **Kibana** | `kibana:8.10.2` | `5601` | http://localhost:5601 |
| **APM Server** | `apm-server:8.10.2` | `8200` | http://localhost:8200 |
| **OTel Collector** | `otel/opentelemetry-collector-contrib:0.103.0` | `4317` (gRPC), `4318` (HTTP) | `localhost:4317/4318` |
| **API Gateway** (local sim) | `nginx:1.27-alpine` | `8000` | http://localhost:8000 |
| **Swagger UI** (aggregator) | `swaggerapi/swagger-ui:v5.17.14` | – (chỉ qua gateway) | http://localhost:8000/ |

---

## API Gateway (local simulation) + Swagger UI

> **Lưu ý:** Ở môi trường **dev/prod**, API Gateway thật là **AWS API Gateway** – config NGINX dưới đây **chỉ dùng để giả lập ở local**, giúp dev có một entrypoint duy nhất giống production (routing theo path, rate limiting) và một trang Swagger UI tổng hợp OpenAPI docs của toàn bộ service.

Container `api-gateway` (NGINX) lắng nghe port `8000`, forward request tới từng service Spring Boot **đang chạy trên host machine** (`mvnw spring-boot:run`) qua `host.docker.internal`, route theo path – giữ nguyên cách gọi API như khi lên AWS API Gateway thật (không thêm prefix tên service):

| Path prefix | Forward tới | Port host |
|---|---|---|
| `/api/v1/events`, `/api/v1/locations` | Event Service | `8081` |
| `/api/v1/tickets` | Ticket Service | `8082` |
| `/api/v1/orders` | Order Service | `8083` |
| `/api/v1/payments` | Payment Service | `8084` |
| `/api/v1/notifications` | Notification Service | `8085` |
| `/api/v1/users` | User Service | `8092` |

Rate limiting cơ bản (giả lập chống bot của gateway thật) áp dụng cho toàn bộ `/api/v1/*`: `20 request/s/IP`, cho phép burst `40` request.

**Swagger UI tổng hợp:** truy cập http://localhost:8000/ để xem OpenAPI docs của cả 6 service trong cùng một giao diện. Gateway proxy `/docs/{service}/v3/api-docs` tới `/v3/api-docs` của từng service (cần service đã chạy và có dependency `springdoc-openapi-starter-webmvc-ui` – đã thêm sẵn vào module `*-application` của cả 6 service).

Cấu hình: `infra/nginx/gateway.conf` (mount vào `/etc/nginx/conf.d/default.conf` của container `api-gateway`).

**Lưu ý khi chạy service:** service phải chạy trên đúng port khai báo trong `application.yaml` (`SERVER_PORT`) để gateway forward đúng; không cần chỉnh gì thêm ở phía service ngoài dependency Swagger đã có sẵn.

---

## Observability Pipeline

```
Spring Boot Services
      │
      │  OTLP gRPC/HTTP (port 4317/4318)
      ▼
[OTel Collector]
      ├── Traces  ──► APM Server (:8200) ──► Elasticsearch ──► Kibana APM
      ├── Logs    ──► Logstash (:5000)   ──► Elasticsearch ──► Kibana Discover
      └── Metrics ──► Elasticsearch (:9200)                ──► Kibana Dashboard

Spring Boot (Logback)
      │
      │  TCP JSON (port 5000) – LogstashTcpSocketAppender
      ▼
[Logstash] ──► Elasticsearch ──► Kibana Discover
```

**Lưu ý**: Spring Boot gửi logs trực tiếp tới Logstash qua TCP (Logback appender).
OTel Collector xử lý traces và metrics, forward logs nếu service dùng OTLP log exporter.

---

## Khởi chạy

```bash
# Từ thư mục d:\EasyTicket
docker compose up -d

# Xem trạng thái
docker compose ps

# Xem logs của một service cụ thể
docker compose logs -f otel-collector
docker compose logs -f apm-server
docker compose logs -f keycloak
```

## Dừng / Xóa

```bash
# Dừng nhưng giữ data
docker compose down

# Dừng và xóa toàn bộ data (volumes)
docker compose down -v
```

---

## Databases có sẵn (MySQL)

| Database | Service sử dụng |
|---|---|
| `keycloak_db` | Keycloak identity provider |
| `event_db` | Event Service |
| `order_db` | Order Service |
| `ticket_db` | Ticket Service |
| `payment_db` | Payment Service |
| `notification_db` | Notification Service |
| `user_db` | (alias, dự phòng) |

**Root credentials:** `root` / `password`  
**App user credentials:** `easyticket` / `easyticket_password`

---

## Keycloak Setup (sau lần đầu chạy)

1. Truy cập http://localhost:8080
2. Đăng nhập: `admin` / `admin`
3. Tạo Realm mới: `SonNS_realm`
4. Trong realm, tạo Client: `quan_ly_ke_toan`
   - Client type: `OpenID Connect`
   - Client authentication: `On` (confidential)
   - Copy client secret vào `application.yaml`
5. Tạo roles: `ORGANIZER`, `TICKET_BUYER`, `SYSTEM_ADMIN`
6. Tạo user test và gán role

---

## Kafka Topics (tạo tự động)

| Topic | Partitions | Producer | Consumer |
|---|---|---|---|
| `ticket-reserved` | 3 | Ticket Service | Order Service |
| `payment-success` | 3 | Payment Service | Order, Notification |
| `payment-failed` | 3 | Payment Service | Order, Ticket |

Xem messages tại Kafka UI: http://localhost:8086

---

## Kibana – Xem Observability Data

### Setup lần đầu

1. Truy cập http://localhost:5601
2. Vào **Stack Management** → **Index Patterns**
3. Tạo các index patterns:
   - `easyticket-*` → cho logs (field thời gian: `@timestamp`)
   - `otel-metrics` → cho metrics

### Logs

- Menu **Discover** → chọn index pattern `easyticket-*`
- Filter theo `service_name`, `level`, `traceId`
- Click vào `traceId` để link sang APM trace tương ứng

### Traces & Service Map

- Menu **Observability** → **APM**
- Xem Services, Transactions, Traces, Service Map
- Correlation tự động với logs qua `traceId`

### Metrics

- Menu **Discover** → chọn index pattern `otel-metrics`
- Hoặc tạo Dashboard từ Kibana Lens với các JVM, HTTP metrics

---

## Cấu hình Spring Boot Service

### 1. Thêm dependencies (`pom.xml`)

```xml
<!-- Logstash JSON appender -->
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
<!-- OpenTelemetry tracing -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-otel</artifactId>
</dependency>
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-exporter-otlp</artifactId>
</dependency>
<!-- Prometheus metrics (scraped bởi OTel Collector) -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

### 2. `application.yaml`

```yaml
management:
  tracing:
    sampling:
      probability: 1.0
  otlp:
    tracing:
      endpoint: http://otel-collector:4318/v1/traces
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

### 3. `logback-spring.xml`

```xml
<appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
    <destination>logstash:5000</destination>
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
        <includeMdcKeyName>traceId</includeMdcKeyName>
        <includeMdcKeyName>spanId</includeMdcKeyName>
        <customFields>{"service":"${spring.application.name:-unknown}"}</customFields>
    </encoder>
    <keepAliveDuration>5 minutes</keepAliveDuration>
</appender>
```

---

## Redis Connection

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: easyticket_redis
```

---

## Troubleshooting

**Kafka không kết nối được từ ứng dụng Spring Boot:**  
Đảm bảo `bootstrap-servers: localhost:9092` trong `application.yaml`. Kafka expose `PLAINTEXT_HOST` listener ra port 9092 cho host machine.

**Keycloak chờ lâu khi khởi động:**  
Keycloak khởi tạo schema vào MySQL mất ~60s. Chờ log `Keycloak 26.0.4 on JVM` xuất hiện.

**Elasticsearch báo lỗi `vm.max_map_count`:**  
Chạy lệnh sau trên WSL2 / Linux host:
```bash
sudo sysctl -w vm.max_map_count=262144
```

**OTel Collector không nhận traces:**  
Kiểm tra service đã cấu hình đúng endpoint chưa:
```yaml
management:
  otlp:
    tracing:
      endpoint: http://otel-collector:4318/v1/traces
```
Xem logs: `docker compose logs -f otel-collector`

**APM Server không hiển thị traces trong Kibana:**  
APM Server cần Kibana sẵn sàng trước khi khởi động. Nếu lỗi, restart APM Server:
```bash
docker compose restart apm-server
```

**Logs không xuất hiện trong Kibana:**  
Kiểm tra Logstash nhận được data: `docker compose logs -f logstash`  
Đảm bảo index pattern `easyticket-*` đã được tạo trong Kibana.
