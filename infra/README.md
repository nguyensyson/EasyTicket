# EasyTicket – Local Infrastructure

## Tổng quan

| Service | Image | Port | URL / Connection |
|---|---|---|---|
| **MySQL** | `mysql:8.0` | `3306` | `jdbc:mysql://localhost:3306/{db}` |
| **Redis** | `redis:7-alpine` | `6379` | `redis://localhost:6379` |
| **Zookeeper** | `wurstmeister/zookeeper:3.4.6` | `2181` | `localhost:2181` |
| **Kafka** | `wurstmeister/kafka:2.13-2.8.1` | `9092` | `localhost:9092` |
| **Kafka UI** | `provectuslabs/kafka-ui:latest` | `8085` | http://localhost:8085 |
| **Keycloak** | `quay.io/keycloak/keycloak:26.0.4` | `8080` | http://localhost:8080 |
| **Elasticsearch** | `elasticsearch:8.10.2` | `9200` | http://localhost:9200 |
| **Logstash** | `logstash:8.10.0` | `5000`, `5044` | `localhost:5000` (TCP) |
| **Kibana** | `kibana:8.10.2` | `5601` | http://localhost:5601 |

---

## Khởi chạy

```bash
# Từ thư mục d:\EasyTicket
docker compose up -d

# Xem trạng thái
docker compose ps

# Xem logs của một service cụ thể
docker compose logs -f keycloak
docker compose logs -f kafka
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
| `pro_be_auth` | AuthService (hiện tại) |
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

Xem messages tại Kafka UI: http://localhost:8085

---

## Kibana – Xem Logs

1. Truy cập http://localhost:5601
2. Vào **Stack Management** → **Index Patterns**
3. Tạo index pattern: `easyticket-*`
4. Vào **Discover** để xem logs

**Logs từ Spring Boot được gửi đến Logstash qua TCP port 5000.**  
Thêm dependency vào `pom.xml` của service:

```xml
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```

Thêm appender vào `logback-spring.xml`:

```xml
<appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
    <destination>localhost:5000</destination>
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
        <customFields>{"service_name":"${spring.application.name}"}</customFields>
    </encoder>
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
