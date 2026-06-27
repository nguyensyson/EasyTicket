# EasyTicket – Tiêu chuẩn Kỹ thuật cho Microservice

> Tài liệu này định nghĩa các tiêu chuẩn kỹ thuật áp dụng thống nhất cho toàn bộ hệ thống EasyTicket.
> Kế thừa từ **AuthService** và bổ sung các best practices cho hệ thống Spring Boot Microservice production.

---

## 1. Logging

### 1.1 Thư viện & Cấu hình

Sử dụng **SLF4J + Logback** (mặc định của Spring Boot). Không dùng `System.out.println`.

```java
// Đúng – dùng SLF4J
private static final Logger log = LoggerFactory.getLogger(UsersServiceImpl.class);
// Hoặc dùng Lombok @Slf4j annotation
@Slf4j
@Service
public class UsersServiceImpl implements UsersService { }
```

Cấu hình trong `application.yaml`:

```yaml
logging:
  level:
    root: INFO
    com.easytickets: DEBUG               # Debug toàn bộ code nội bộ
    org.hibernate.SQL: DEBUG     # Log SQL queries (chỉ bật ở dev/local)
    org.hibernate.type: TRACE    # Log SQL params (chỉ bật ở dev/local)
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId},%X{spanId}] %-5level %logger{36} - %msg%n"
```

### 1.2 Log Levels

| Level | Khi nào dùng |
|---|---|
| `ERROR` | Exception không recover được, lỗi hệ thống, tích hợp thất bại |
| `WARN` | Tình huống bất thường nhưng hệ thống vẫn hoạt động, retry, timeout |
| `INFO` | Các sự kiện quan trọng của business flow (user register, order created, payment success) |
| `DEBUG` | Chi tiết kỹ thuật, input/output của các method, chỉ dùng trong dev |
| `TRACE` | SQL params, raw HTTP body – chỉ dùng khi debug cụ thể |

### 1.3 Thông tin Bắt buộc trong Log

Mỗi log entry phải có:
- **Timestamp** – tự động qua log pattern
- **Thread name** – tự động qua log pattern
- **traceId / spanId** – inject từ OpenTelemetry MDC (xem mục 2)
- **Log level**
- **Logger name** (tên class)
- **Message** – mô tả rõ ràng, kèm context (userId, orderId, v.v.)

```java
// Đúng – kèm context
log.info("User registered successfully. userId={}, email={}", userId, email);
log.error("Failed to create user in Keycloak. username={}, status={}", username, status);

// Sai – thiếu context
log.info("User registered");
log.error("Error");
```

### 1.4 Dữ liệu KHÔNG được ghi log

- Password, client_secret, private key, token bất kỳ dạng nào
- Số thẻ tín dụng, thông tin thanh toán đầy đủ
- Personal Identifiable Information (PII) như CCCD, số điện thoại đầy đủ
- Refresh token, access token (chỉ log token ID nếu cần)

```java
// Sai
log.info("Logout request. refreshToken={}", refreshToken);

// Đúng
log.info("Logout requested. clientId={}", keycloakConfig.getClientId());
```

---

## 2. OpenTelemetry & Distributed Tracing

### 2.1 Dependencies

Thêm vào parent POM hoặc từng service cần tracing:

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-otel</artifactId>
</dependency>
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-exporter-otlp</artifactId>
</dependency>
```

### 2.2 Cấu hình trong `application.yaml`

```yaml
management:
  tracing:
    sampling:
      probability: 1.0   # 100% sampling (giảm xuống 0.1 ở production)
  otlp:
    tracing:
      endpoint: http://jaeger:4318/v1/traces
spring:
  application:
    name: AuthService-application   # Tên này xuất hiện trong trace
```

### 2.3 TraceId / SpanId trong Log

Khi dùng `micrometer-tracing`, `traceId` và `spanId` được tự động inject vào MDC.
Log pattern phải bao gồm `%X{traceId}` và `%X{spanId}`:

```
"%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId},%X{spanId}] %-5level %logger{36} - %msg%n"
```

Output mẫu:
```
2025-04-05 16:52:00.123 [http-nio-8091-exec-1] [4bf92f3577b34da6a3ce929d0e0e4736,00f067aa0ba902b7] INFO  s.h.b.s.i.UsersServiceImpl - User registered successfully. userId=abc123
```

### 2.4 Trace Propagation giữa các Service

Khi một service gọi service khác qua REST (Feign Client hoặc RestTemplate), trace context phải được propagate tự động qua HTTP headers (`traceparent`, `tracestate` theo W3C standard).

- **Feign Client**: tự động propagate khi dùng `micrometer-tracing`.
- **RestTemplate**: inject `ObservationRestTemplateCustomizer` bean.
- **Kafka**: dùng `TracingKafkaProducerFactory` / `TracingKafkaConsumerFactory`.

```java
// Bean để RestTemplate propagate trace (đặt trong Config)
@Bean
public RestTemplate restTemplate(ObservationRegistry registry) {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.setInterceptors(List.of(new ObservationRestTemplateCustomizer(registry)));
    return restTemplate;
}
```

---

## 3. Exception Handling

### 3.1 Exception Hierarchy

```
RuntimeException
└── BusinessException (base – trong business/exception/)
    ├── ResourceNotFoundException     # 404
    ├── ConflictException             # 409 (ví dụ: email đã tồn tại)
    ├── ValidationException           # 400 (business validation, không phải Bean Validation)
    └── {Domain}Exception             # Domain-specific (ví dụ: TicketSoldOutException)
```

Định nghĩa `BusinessException`:

```java
@Getter
public class BusinessException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus httpStatus;

    public BusinessException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
```

### 3.2 Global Exception Handler

Đặt trong `application` module tại `com.easytickets.application.exception`:

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Bean Validation (@Valid) – 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", message);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", message));
    }

    // Business Exception – HTTP status từ exception
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        log.warn("Business error. code={}, message={}", ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.status(ex.getHttpStatus())
                .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage()));
    }

    // Fallback – 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "An unexpected error occurred"));
    }
}
```

### 3.3 Quy tắc xử lý Exception

- **Không bắt exception rồi nuốt im lặng** – luôn log hoặc rethrow.
- **Không throw `RuntimeException` trực tiếp** – dùng `BusinessException` hoặc subclass cụ thể.
- **Infrastructure exception** (DB connection, Kafka, HTTP timeout) → bắt ở Service layer, wrap thành `BusinessException` với message thân thiện, log nguyên bản exception ở level ERROR.
- **Không để stack trace xuất hiện trong response body** – chỉ trả `errorCode` và `message`.
- ServiceImpl trong AuthService hiện throw `IllegalStateException` trực tiếp – các service mới **phải** dùng `BusinessException`.

---

## 4. Base Response (ApiResponse)

### 4.1 Cấu trúc chuẩn

Đặt tại `com.easytickets.application.dto.response.ApiResponse` (hoặc `com.easytickets.common` nếu cần dùng chung):

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String errorCode;    // null khi success
    private String message;
    private T data;              // null khi error
    private String traceId;      // inject từ MDC

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true).data(data).build();
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true).data(data).message(message).build();
    }

    public static ApiResponse<Void> error(String errorCode, String message) {
        return ApiResponse.<Void>builder()
                .success(false).errorCode(errorCode).message(message).build();
    }
}
```

### 4.2 Cách dùng trong Controller

```java
// Success với data
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable String id) {
    UserResponse user = usersService.getById(id);
    return ResponseEntity.ok(ApiResponse.ok(user));
}

// Success không có data
@PostMapping("/register")
public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody CreateUsersRequest request) {
    usersService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(null, "User registered successfully."));
}
```

### 4.3 Cấu trúc Response mẫu

**Thành công:**
```json
{
  "success": true,
  "errorCode": null,
  "message": "User registered successfully.",
  "data": { "id": "abc123", "email": "user@example.com" },
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```

**Lỗi:**
```json
{
  "success": false,
  "errorCode": "USER_ALREADY_EXISTS",
  "message": "Email already registered: user@example.com",
  "data": null,
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```

**Lưu ý**: AuthService hiện trả `ResponseEntity<String>` trực tiếp – các service mới phải dùng `ApiResponse<T>`.

---

## 5. Database & Liquibase

### 5.1 Tổ chức Migration Module

Migration chạy như một Spring Boot app **độc lập**, tách biệt hoàn toàn khỏi application:

```
{ServiceName}-migration/src/main/resources/
├── application.yaml          # datasource + liquibase.enabled=true
└── db/
    ├── changelog/
    │   └── changelog.xml     # Master file – chỉ include, không viết SQL trực tiếp
    └── sources/
        └── {V}{n}_{YYYYMMDDHHmm}_{description}.sql
```

`application.yaml` của migration:
```yaml
spring:
  application:
    name: {ServiceName}-migration
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/{db_name}}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver
  liquibase:
    enabled: true
    change-log: classpath:db/changelog/changelog.xml
```

**Application module** phải tắt Liquibase:
```yaml
spring:
  liquibase:
    enabled: false
```

### 5.2 Cấu trúc `changelog.xml`

```xml
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd">

    <changeSet author="{author}" id="V1_202504051652_create_users_table" onValidationFail="MARK_RAN">
        <sqlFile dbms="mysql"
                 path="classpath:/db/sources/V1_202504051652_create_users_table.sql"
                 relativeToChangelogFile="false"/>
    </changeSet>

    <!-- Thêm changeSet mới ở cuối, KHÔNG sửa changeSet đã có -->
</databaseChangeLog>
```

### 5.3 Naming Convention Migration

| Thành phần | Convention | Ví dụ |
|---|---|---|
| File SQL | `V{n}_{YYYYMMDDHHmm}_{mô tả}.sql` | `V2_202506281000_add_phone_to_users.sql` |
| ChangeSet ID | Trùng tên file (bỏ `.sql`) | `V2_202506281000_add_phone_to_users` |
| Author | `{tên}.{họ}` lowercase | `son.nguyen` |
| Table name | `snake_case`, danh từ số nhiều | `users`, `ticket_orders`, `payment_transactions` |
| Column name | `snake_case` | `created_at`, `keycloak_id`, `deleted_at` |
| Audit columns bắt buộc | `id`, `created_at`, `updated_at`, `deleted` | |

### 5.4 Nguyên tắc quản lý Schema

- **KHÔNG sửa changeSet đã commit** – chỉ thêm changeSet mới.
- **KHÔNG dùng `DROP TABLE` trong migration thực tế** – chỉ dùng khi reset dev env.
- Soft delete qua cột `deleted ENUM('ACTIVE', 'DELETED') DEFAULT 'ACTIVE'`.
- Primary key dùng `CHAR(36)` với `DEFAULT (UUID())` – consistent với `@GeneratedValue(strategy = GenerationType.UUID)`.
- Mỗi bảng cần đủ: `id`, `created_at`, `updated_at`, `deleted`.
- `onValidationFail="MARK_RAN"` – luôn thêm attribute này để tránh lỗi checksum khi hotfix.

### 5.5 BaseEntity (nên tạo trong `infratructures/model/`)

```java
@MappedSuperclass
@Data
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeleteStatus deleted = DeleteStatus.ACTIVE;
}
```

Entity extend `BaseEntity`:
```java
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class Users extends BaseEntity {
    private String name;
    @Column(unique = true)
    private String email;
    private String username;
    private String keycloakId;
}
```

---

## 6. Configuration Management

### 6.1 `application.yaml` chuẩn

Mỗi service có `application.yaml` trong `{ServiceName}-application/src/main/resources/`:

```yaml
spring:
  application:
    name: {ServiceName}-application
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/{db_name}}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    properties:
      hibernate:
        show_sql: false    # false ở production, true chỉ ở local
  liquibase:
    enabled: false         # Migration chạy qua module riêng
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: ${KEYCLOAK_JWK_URI:http://localhost:8080/realms/{realm}/protocol/openid-connect/certs}
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: ${spring.application.name}
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      auto-offset-reset: earliest

management:
  tracing:
    sampling:
      probability: ${TRACING_SAMPLING:1.0}
  otlp:
    tracing:
      endpoint: ${OTEL_EXPORTER_OTLP_ENDPOINT:http://jaeger:4318/v1/traces}
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus

server:
  port: ${SERVER_PORT:8091}

keycloak:
  realm: ${KEYCLOAK_REALM:SonNS_realm}
  server-url: ${KEYCLOAK_SERVER_URL:http://localhost:8080}
  client-id: ${KEYCLOAK_CLIENT_ID:quan_ly_ke_toan}
  client-secret: ${KEYCLOAK_CLIENT_SECRET:changeme}

logging:
  level:
    root: INFO
    com.easytickets: DEBUG
    org.hibernate.SQL: DEBUG

url:
  permit:
    - path: "api/v1/register"
      methods: ["POST"]
```

### 6.2 Profiles

| Profile | Mục đích | Cách kích hoạt |
|---|---|---|
| `local` | Dev trên máy cá nhân | `SPRING_PROFILES_ACTIVE=local` |
| `dev` | Môi trường dev/staging | `SPRING_PROFILES_ACTIVE=dev` |
| `prod` | Production | `SPRING_PROFILES_ACTIVE=prod` |

Đặt profile-specific config tại `application-{profile}.yaml`. Ví dụ `application-prod.yaml`:
```yaml
logging:
  level:
    root: WARN
    com.easytickets: INFO
    org.hibernate.SQL: OFF
spring:
  jpa:
    properties:
      hibernate:
        show_sql: false
management:
  tracing:
    sampling:
      probability: 0.1
```

### 6.3 Biến môi trường & Secret Management

**Quy tắc:**
- **KHÔNG hardcode** credentials, passwords, secrets trong source code hoặc `application.yaml`.
- Mọi giá trị nhạy cảm dùng syntax `${ENV_VAR:default_value}` – default chỉ dùng cho local dev.
- Trên Kubernetes: inject qua **ConfigMap** (config không nhạy cảm) và **Secret** (passwords, tokens).
- Biến môi trường quan trọng cần document trong `README.md` của mỗi service.

```yaml
# Đúng
keycloak:
  client-secret: ${KEYCLOAK_CLIENT_SECRET:changeme}

# Sai
keycloak:
  client-secret: 2hew13lEtOYUIlvA6sGuhuEZrpO8eYHz
```

### 6.4 ConfigurationProperties – Typed Config

Thay vì inject từng `@Value`, dùng `@ConfigurationProperties` như AuthService đang làm:

```java
@Configuration
@ConfigurationProperties(prefix = "keycloak")
@Getter @Setter
public class KeycloakConfigProperties {
    private String serverUrl;
    private String realm;
    private String clientId;
    private String clientSecret;
}
```

Kích hoạt tại config class hoặc `@SpringBootApplication`:
```java
@EnableConfigurationProperties(KeycloakConfigProperties.class)
```

---

## 7. Security Standards

### 7.1 OAuth2 Resource Server (JWT)

Tất cả service (trừ Public API) đều là **OAuth2 Resource Server** xác thực JWT từ Keycloak.

```java
// SecurityConfig.java – chuẩn từ AuthService
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

    private final SecurityProperties securityProperties;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> {
                securityProperties.getPermit().forEach(api ->
                    api.getMethods().forEach(method ->
                        auth.requestMatchers(HttpMethod.valueOf(method), api.getPath()).permitAll()
                    )
                );
                auth.anyRequest().authenticated();
            })
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(new CustomJwtAuthenticationConverter()))
            );
        return http.build();
    }
}
```

### 7.2 Permit List Configuration

Danh sách endpoint public cấu hình trong `application.yaml`, không hardcode trong SecurityConfig:

```yaml
url:
  permit:
    - path: "api/v1/register"
      methods: ["POST"]
    - path: "api/v1/logout"
      methods: ["POST"]
    - path: "/actuator/health"
      methods: ["GET"]
```

### 7.3 JWT Claims & Role Extraction

`CustomJwtAuthenticationConverter` extract roles từ Keycloak claim `resource_access.{client-id}.roles`:

```java
// Roles được prefix "ROLE_" để tương thích với Spring Security
List<GrantedAuthority> authorities = roles.stream()
    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
    .collect(Collectors.toList());
```

Dùng `@PreAuthorize` để bảo vệ endpoint theo role:
```java
@PreAuthorize("hasRole('ORGANIZER')")
@PostMapping("/events")
public ResponseEntity<ApiResponse<EventResponse>> createEvent(...) { }
```

### 7.4 Service-to-Service Authentication

Khi một service gọi service khác nội bộ:
- Dùng Keycloak **Client Credentials flow** để lấy token.
- Truyền token qua `Authorization: Bearer {token}` header.
- Không bypass authentication cho internal calls.

---

## 8. Kafka Standards

### 8.1 Message Format

Mọi Kafka message dùng **JSON**. Định nghĩa message class trong `business/dto/event/`:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketReservedEvent {
    private String eventId;
    private String ticketId;
    private String userId;
    private String orderId;
    private LocalDateTime reservedAt;
}
```

### 8.2 Producer

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class TicketReservedProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(TicketReservedEvent event) {
        log.info("Publishing ticket-reserved event. ticketId={}, userId={}", 
                 event.getTicketId(), event.getUserId());
        kafkaTemplate.send("ticket-reserved", event.getTicketId(), event);
    }
}
```

### 8.3 Consumer

```java
@Component
@Slf4j
@RequiredArgsConstructor
public class TicketReservedConsumer {

    @KafkaListener(topics = "ticket-reserved", groupId = "${spring.application.name}")
    public void consume(TicketReservedEvent event) {
        log.info("Received ticket-reserved. ticketId={}, orderId={}", 
                 event.getTicketId(), event.getOrderId());
        // Idempotency check trước khi xử lý
        orderService.createFromReservation(event);
    }
}
```

### 8.4 Nguyên tắc Kafka

- **Idempotency**: Consumer phải kiểm tra xem message đã xử lý chưa (dùng `orderId` hoặc `eventId` làm idempotency key, lưu vào DB).
- **Dead Letter Queue**: Cấu hình DLQ cho consumer – message lỗi sau N retry sẽ chuyển sang topic `{topic}.DLT`.
- **Message key**: Dùng ID của entity chính làm partition key để đảm bảo ordering.
- **Không block consumer** bằng long-running operation – xử lý async nếu cần.

---

## 9. API Standards

### 9.1 Versioning

Tất cả API đều có prefix version: `api/v1/...` (kế thừa từ AuthController).

```java
@RestController
@RequestMapping("api/v1/")
public class AuthController { }
```

Khi breaking change, tạo controller mới với `api/v2/...`, giữ `v1` cho backward compatibility.

### 9.2 HTTP Method Conventions

| Operation | Method | Ví dụ |
|---|---|---|
| Tạo resource | `POST` | `POST api/v1/events` |
| Lấy danh sách | `GET` | `GET api/v1/events` |
| Lấy chi tiết | `GET` | `GET api/v1/events/{id}` |
| Cập nhật toàn bộ | `PUT` | `PUT api/v1/events/{id}` |
| Cập nhật một phần | `PATCH` | `PATCH api/v1/events/{id}` |
| Xóa | `DELETE` | `DELETE api/v1/events/{id}` |

### 9.3 Health Check

Mọi service expose endpoint `/actuator/health` – dùng cho Kubernetes liveness/readiness probe:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
```

---

## 10. Inter-Service Communication (Feign Client)

Khi service A cần gọi HTTP đồng bộ đến service B, dùng **OpenFeign**:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

```java
// Đặt trong business/client/
@FeignClient(name = "event-service", url = "${services.event-service.url}")
public interface EventServiceClient {
    @GetMapping("/api/v1/events/{id}")
    ApiResponse<EventResponse> getEvent(@PathVariable String id);
}
```

Kích hoạt tại Application:
```java
@EnableFeignClients(basePackages = "com.easytickets")
```

Cấu hình URL trong `application.yaml`:
```yaml
services:
  event-service:
    url: ${EVENT_SERVICE_URL:http://event-service:8092}
```

---

## 11. Audit Logging

Các event quan trọng của business cần được log ở level `INFO` với đủ context:

| Event | Log message pattern |
|---|---|
| User register | `User registered. userId={}, email={}` |
| User logout | `User logged out. clientId={}` |
| Order created | `Order created. orderId={}, userId={}, eventId={}` |
| Payment success | `Payment successful. paymentId={}, orderId={}, amount={}` |
| Payment failed | `Payment failed. paymentId={}, orderId={}, reason={}` |
| Ticket reserved | `Ticket reserved. ticketId={}, userId={}, eventId={}` |
| Ticket released | `Ticket released (payment failed). ticketId={}, orderId={}` |

Không log object toàn bộ – chỉ log các ID và trạng thái quan trọng.

---

## 12. Dependency Versions (Parent POM)

Các version được quản lý tập trung trong Parent POM `<dependencyManagement>`:

| Dependency | Version |
|---|---|
| Spring Boot | `3.4.4` |
| Java | `17` (compile target `21` trong module) |
| Lombok | `1.18.30` |
| MapStruct | `1.6.3` |
| Liquibase | `4.30.0` |
| Hibernate Validator | `8.0.1.Final` |
| Keycloak Admin Client | `26.0.4` |
| Spring Security | `6.4.3` |
| MySQL Connector | Managed by Spring Boot BOM |

Khi thêm dependency mới:
1. Thêm vào `<dependencyManagement>` trong Parent POM với version cụ thể.
2. Dùng trong module con **không kèm version**.
3. Ưu tiên dependencies đã có trong Spring Boot BOM trước khi thêm mới.
