# EasyTicket – Tiêu chuẩn Kỹ thuật

## 1. Dependency versions (chuẩn dự án)

| Dependency | Version |
|---|---|
| Spring Boot | `4.0.7` |
| Java | `21` |
| Build tool | Maven (multi-module per service) |
| Lombok | `1.18.30` |
| MapStruct | `1.6.3` |
| Liquibase | `4.30.0` |
| Keycloak Admin Client | `26.0.4` |
| Logstash Logback Encoder | `7.4` |
| Spring Cloud OpenFeign | `4.2.1` |
| Spring Security, MySQL Connector | Managed bởi Spring Boot BOM |

Version quản lý tập trung trong Parent POM `<dependencyManagement>` của mỗi service – không pin version lẻ tẻ trong module con.

## 2. Logging

Dùng **SLF4J + Logback** (mặc định Spring Boot). **Không bao giờ** dùng `System.out.println`.

```java
@Slf4j
@Service
public class UsersServiceImpl implements UsersService { }
```

| Level | Khi nào dùng |
|---|---|
| `ERROR` | Exception không recover được, lỗi hệ thống, tích hợp thất bại |
| `WARN` | Bất thường nhưng hệ thống vẫn chạy, retry, timeout |
| `INFO` | Business event quan trọng (register, order created, payment success) |
| `DEBUG` | Chi tiết kỹ thuật, chỉ dùng dev |
| `TRACE` | SQL params, raw HTTP body |

Mỗi log entry **phải** kèm context (`userId`, `orderId`, ...), không log chay:

```java
log.info("User registered successfully. userId={}, email={}", userId, email);
```

**Không bao giờ log**: password, client_secret, private key, access/refresh token, số thẻ, PII đầy đủ (CCCD, SĐT).

## 3. Observability (OpenTelemetry)

Pipeline: `Spring Boot → OTel Collector (OTLP) → Traces → APM Server → Elasticsearch → Kibana`, tương tự cho Logs (qua Logstash) và Metrics.

- `traceId`/`spanId` tự động inject vào MDC qua `micrometer-tracing`, xuất hiện trong cả console log và JSON log gửi Logstash.
- Trace context propagate qua HTTP header W3C (`traceparent`) tự động với Feign; RestTemplate cần bean `ObservationRestTemplateCustomizer`; Kafka dùng `TracingKafkaProducerFactory`/`ConsumerFactory`.
- `management.tracing.sampling.probability`: `1.0` ở local/dev, hạ xuống `0.1` ở prod.
- Mọi service expose `/actuator/prometheus` để OTel Collector scrape.
- `logback-spring.xml` gửi log JSON qua `LogstashTcpSocketAppender` kèm `traceId`/`spanId`.

## 4. Exception Handling

Hierarchy bắt buộc:

```
RuntimeException
└── BusinessException (base, đặt trong business/exception/)
    ├── ResourceNotFoundException     # 404
    ├── ConflictException             # 409
    ├── ValidationException           # 400 (business validation)
    └── {Domain}Exception             # ví dụ TicketSoldOutException
```

`BusinessException` mang `errorCode` + `HttpStatus`. Xử lý tập trung qua `@RestControllerAdvice` (`GlobalExceptionHandler`) đặt trong module `userServiceApplication`.

**Quy tắc bắt buộc:**
- Không bắt exception rồi nuốt im lặng – luôn log hoặc rethrow.
- Không throw `RuntimeException` trần – dùng `BusinessException` hoặc subclass cụ thể.
- Infrastructure exception (DB, Kafka, HTTP timeout) → bắt ở Service layer, wrap thành `BusinessException` với message thân thiện, log bản gốc ở `ERROR`.
- Không để stack trace lộ trong response body – chỉ trả `errorCode` + `message`.

## 5. ApiResponse (Base Response chuẩn)

Đặt tại `com.easytickets.common.dto.ApiResponse` (dùng chung được mọi module, đây là vị trí chuẩn hiện tại – xem `UserService-common`).

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String errorCode;   // null khi success
    private String message;
    private T data;             // null khi error
    private String traceId;     // inject từ MDC

    public static <T> ApiResponse<T> ok(T data) { ... }
    public static <T> ApiResponse<T> ok(T data, String message) { ... }
    public static ApiResponse<Void> error(String errorCode, String message) { ... }
}
```

Controller luôn trả `ResponseEntity<ApiResponse<T>>`, không bao giờ trả Entity hay kiểu thô trực tiếp.

## 6. Database & Liquibase

- Mỗi service sở hữu database riêng (Database per Service).
- Migration chạy qua module `{ServiceName}-migration` **độc lập** với application (`spring.liquibase.enabled=false` ở application, `=true` ở migration).
- File SQL: `V{n}_{YYYYMMDDHHmm}_{description}.sql`, đặt tại `db/sources/`; `changelog.xml` chỉ include, không viết SQL trực tiếp.
- ChangeSet ID = tên file (bỏ `.sql`); luôn thêm `onValidationFail="MARK_RAN"`.
- **Không sửa changeSet đã commit** – chỉ thêm mới. **Không `DROP TABLE`** trong migration thực tế.
- Table: `snake_case` số nhiều (`ticket_orders`). Column: `snake_case`. Primary key: `CHAR(36) DEFAULT (UUID())`.

### Audit fields & Soft delete – bắt buộc cho MỌI Entity

- Không bao giờ xóa vật lý dữ liệu. "Xóa" = set `delete_flag = DELETED`.
- Mọi query mặc định chỉ lấy `delete_flag = ACTIVE` (dùng `@Where` annotation hoặc filter tường minh ở repo).
- `createdBy`/`updatedBy` chỉ lưu Keycloak User ID (JWT claim `sub`), không lưu username/email.
- `createdAt/updatedAt/createdBy/updatedBy` gán tự động qua Spring Data JPA Auditing (`@EnableJpaAuditing` + `AuditorAware`) – **không set thủ công** trong Controller/Service.

```java
@MappedSuperclass
@Data
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Enumerated(EnumType.STRING) @Column(name = "delete_flag", nullable = false)
    private RecordStatus deleteFlag = RecordStatus.ACTIVE;
    @CreatedBy @Column(name = "created_by", updatable = false) private String createdBy;
    @CreatedDate @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @LastModifiedBy @Column(name = "updated_by") private String updatedBy;
    @LastModifiedDate @Column(name = "updated_at") private LocalDateTime updatedAt;
}
```

Migration SQL của mọi bảng mới phải có đủ 6 cột audit + `id` như trên. `RecordStatus` = `enum { ACTIVE, DELETED }`.

Khi sinh Entity/Migration/Repository/Service mới, **tự động** áp dụng chuẩn này mà không cần nhắc lại.

## 7. Configuration Management

- **Không hardcode** credentials/secrets trong code hay `application.yaml`. Luôn dùng `${ENV_VAR:default}` (default chỉ dùng local dev).
- Kubernetes: config không nhạy cảm qua ConfigMap, secret qua Secret.
- Dùng `@ConfigurationProperties` thay vì `@Value` rời rạc cho config có cấu trúc (ví dụ `KeycloakConfigProperties`).
- 3 profile: `local` (dev máy cá nhân), `dev` (staging), `prod`. Đặt tại `application-{profile}.yaml`. Ở `prod`: `hibernate.show_sql=false`, log level `WARN/INFO`, tracing sampling `0.1`.

## 8. Security

- Mọi service (trừ public API) là **OAuth2 Resource Server** xác thực JWT từ Keycloak.
- Danh sách endpoint public cấu hình trong `application.yaml` (`url.permit`), **không hardcode** trong `SecurityConfig`.
- Role extract từ claim `resource_access.{client-id}.roles`, prefix `ROLE_` để tương thích Spring Security; bảo vệ endpoint bằng `@PreAuthorize("hasRole('...')")`.
- Gọi service nội bộ: dùng Keycloak Client Credentials flow lấy token, truyền `Authorization: Bearer {token}` – không bypass authentication cho internal call.

## 9. Kafka Standards

- Message format: **JSON**, định nghĩa class trong `business/dto/event/`.
- **Idempotency bắt buộc**: consumer phải kiểm tra message đã xử lý chưa (dùng `orderId`/`eventId` làm idempotency key lưu DB) trước khi xử lý.
- **Dead Letter Queue**: message lỗi sau N lần retry chuyển sang topic `{topic}.DLT`.
- Message key = ID entity chính, đảm bảo ordering theo partition.
- Không block consumer bằng long-running operation.

## 10. API Standards

- Versioning bắt buộc: `api/v1/...`. Breaking change → tạo controller `api/v2/...`, giữ `v1` backward-compatible.
- HTTP method: `POST` tạo, `GET` đọc, `PUT` cập nhật toàn bộ, `PATCH` cập nhật một phần, `DELETE` xóa (soft delete).
- Mọi service expose `/actuator/health` cho Kubernetes liveness/readiness probe.

## 11. Inter-Service Communication

Gọi HTTP đồng bộ giữa service dùng **OpenFeign**, client interface đặt trong `business/client/`:

```java
@FeignClient(name = "event-service", url = "${services.event-service.url}")
public interface EventServiceClient {
    @GetMapping("/api/v1/events/{id}")
    ApiResponse<EventResponse> getEvent(@PathVariable String id);
}
```

URL service khác luôn cấu hình qua env var (`${EVENT_SERVICE_URL:...}`), không hardcode.

## 12. Quy tắc sinh code

**Không tạo test khi sinh/sửa code** – chỉ tạo production code (`*Test.java`, `*Tests.java`, file trong `src/test/`, mock/stub đều không tự sinh). Test chỉ viết khi được yêu cầu cụ thể, dùng JUnit 5 + Mockito.
