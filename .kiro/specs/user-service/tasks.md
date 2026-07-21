# Implementation Plan: UserService

## Overview

Implement the UserService microservice for the EasyTicket platform as a Maven multi-module Spring Boot 3.4.4 project. The service delegates authentication to Keycloak, owns the `user_profiles` MySQL table, exposes self-service and admin registration endpoints, profile management, and cross-service aggregation via Feign clients to Order Service and Event Service.

Build order follows the standard EasyTicket module dependency direction:
`common` → `migration` → `business` → `infratructures` → `userServiceApplication` → `worker`

---

## Tasks

- [x] 1. Scaffold Maven multi-module project structure
  - Create the root `UserService/pom.xml` declaring all six modules: `UserService-application`, `UserService-business`, `UserService-common`, `UserService-infratructures`, `UserService-migration`, `UserService-worker`
  - Set `<groupId>com.easytickets</groupId>`, `<packaging>pom</packaging>`, Spring Boot parent `3.4.4`
  - Define `<properties>`: `java.version=17`, compile target `21`, `mapstruct.version=1.6.3`, `liquibase.version=4.30.0`, `keycloak.version=26.0.4`, `lombok.version=1.18.30`
  - Add `<dependencyManagement>` for all shared dependency versions (Spring Boot BOM, Keycloak admin client, MapStruct, Logstash encoder, jqwik, Micrometer OTel, etc.)
  - Configure `maven-compiler-plugin` annotation processor paths for Lombok + MapStruct + `lombok-mapstruct-binding`
  - Create per-module `pom.xml` files with correct internal `<dependency>` references following the module dependency matrix
  - Create standard Maven source directory skeletons under each module (`src/main/java`, `src/main/resources`, `src/test/java`)
  - _Requirements: 12.1, 12.2, 12.3, 12.6_

- [ ] 2. Implement `UserService-common` module
  - [ ] 2.1 Create `ApiResponse<T>` class in `com.easytickets.common.dto`
    - Fields: `boolean success`, `String errorCode`, `String message`, `T data`, `String traceId`
    - Static factory methods: `ok(T data)`, `ok(T data, String message)`, `error(String errorCode, String message)`
    - Annotate with `@Data @Builder @NoArgsConstructor @AllArgsConstructor`
    - _Requirements: 10.1, 10.2, 10.3_

  - [ ] 2.2 Create `AppConstants` class in `com.easytickets.common.constant`
    - Define all error code string constants: `VALIDATION_ERROR`, `INVALID_CREDENTIALS`, `INVALID_TOKEN`, `FORBIDDEN`, `PROFILE_NOT_FOUND`, `USER_ALREADY_EXISTS`, `REGISTRATION_FAILED`, `KEYCLOAK_UNAVAILABLE`, `ORDER_SERVICE_UNAVAILABLE`, `EVENT_SERVICE_UNAVAILABLE`, `INTERNAL_SERVER_ERROR`
    - _Requirements: 10.3, 10.5_

- [ ] 3. Implement `UserService-migration` module
  - [ ] 3.1 Create `application.yaml` for the migration module
    - Configure datasource, `liquibase.enabled=true`, `change-log: classpath:db/changelog/changelog.xml`, `jpa.hibernate.ddl-auto=none`
    - _Requirements: 13.1, 13.3, 13.4_

  - [ ] 3.2 Create `changelog.xml` and `V1_202506280000_create_user_profiles_table.sql`
    - `changelog.xml` master file under `src/main/resources/db/changelog/` — include one `<changeSet>` referencing the SQL file via `<sqlFile>`, with `onValidationFail="MARK_RAN"`
    - SQL file at `src/main/resources/db/sources/V1_202506280000_create_user_profiles_table.sql`
    - SQL creates `user_profiles` table with columns: `id CHAR(36) NOT NULL DEFAULT (UUID())`, `full_name VARCHAR(100) NOT NULL`, `phone VARCHAR(20)`, `avatar_url VARCHAR(255)`, `address VARCHAR(255)`, `delete_flag ENUM('ACTIVE','DELETED') NOT NULL DEFAULT 'ACTIVE'`, `created_by VARCHAR(255)`, `created_at TIMESTAMP`, `updated_by VARCHAR(255)`, `updated_at TIMESTAMP`, `PRIMARY KEY (id)`, engine `InnoDB`, charset `utf8mb4_unicode_ci`
    - Create a `MigrationApplication.java` entry point in the migration module
    - _Requirements: 9.1, 9.2, 13.1, 13.2, 13.5, 13.6_

- [ ] 4. Implement `UserService-business` module – exceptions, DTOs, ports, and Keycloak config
  - [ ] 4.1 Create `BusinessException` hierarchy in `com.easytickets.business.exception`
    - `BusinessException` base extending `RuntimeException` with fields `String errorCode` and `HttpStatus httpStatus`
    - Subclasses: `ValidationException` (400), `UnauthorizedException` (401), `ForbiddenException` (403), `ProfileNotFoundException` (404), `UserAlreadyExistsException` (409), `RegistrationFailedException` (500), `KeycloakUnavailableException` (502), `OrderServiceUnavailableException` (502), `EventServiceUnavailableException` (502)
    - Each subclass calls `super(errorCode, message, httpStatus)` with the correct constant
    - _Requirements: 10.3, 12.5_

  - [ ] 4.2 Create request/response DTOs in `com.easytickets.business.dto`
    - `LoginRequest`: `@NotBlank @Size(max=255) String username`, `@NotBlank @Size(max=255) String password`
    - `LoginResponse`: `String accessToken`, `String refreshToken`, `List<String> roles`
    - `RegisterRequest`: `@NotBlank @Size(min=3,max=50) String username`, `@NotBlank @Size(min=8,max=128) String password`, `@NotBlank @Email @Size(max=255) String email`, `@NotBlank @Size(min=1,max=100) String fullName`
    - `RegisterResponse`: `String id`
    - `UserProfileDto`: `id`, `fullName`, `phone`, `avatarUrl`, `address`, `deleteFlag`, `createdBy`, `createdAt`, `updatedBy`, `updatedAt`
    - `UserProfileResponse`: `id`, `fullName`, `phone`, `avatarUrl`, `address`
    - `UpdateProfileRequest`: `@Size(min=1,max=100) String fullName`, `@Size(max=20) String phone`, `@Size(max=255) String avatarUrl`, `@Size(max=255) String address` — all optional (nullable)
    - Add `UserRole` enum with values `BUYER`, `ORGANIZER`, `ADMIN`
    - _Requirements: 1.1, 1.4, 2.1, 3.1, 4.1, 6.6–6.9_

  - [ ] 4.3 Create `UserProfileRepo` port interface in `com.easytickets.business.repo`
    - `UserProfileDto save(UserProfileDto profile)`
    - `Optional<UserProfileDto> findActiveById(String id)`
    - `UserProfileDto update(UserProfileDto profile)`
    - _Requirements: 12.3_

  - [ ] 4.4 Create Keycloak config in `com.easytickets.business.config`
    - `KeycloakConfigProperties` with `@ConfigurationProperties(prefix = "keycloak")`: `serverUrl`, `realm`, `clientId`, `clientSecret`
    - `KeycloakConfig` bean producing `Keycloak` via `KeycloakBuilder` using CLIENT_CREDENTIALS grant
    - `RestTemplate` bean with 5 s connect + 5 s read timeout via `SimpleClientHttpRequestFactory`, wired with `ObservationRestTemplateCustomizer` for trace propagation
    - _Requirements: 1.2, 1.6, 11.1_

  - [ ] 4.5 Create Feign clients in `com.easytickets.business.client`
    - `OrderServiceClient` `@FeignClient(name="order-service", url="${services.order-service.url}", configuration=FeignClientConfig.class)` — `@GetMapping("/api/v1/orders/my-tickets")` with `@RequestHeader Authorization`, `@RequestParam page`, `@RequestParam size`
    - `EventServiceClient` `@FeignClient(name="event-service", url="${services.event-service.url}", configuration=FeignClientConfig.class)` — `@GetMapping("/api/v1/events/organizer-history")` with `@RequestHeader Authorization`
    - `FeignClientConfig` setting connect and read timeouts to 5 000 ms
    - _Requirements: 7.3, 8.4_

  - [ ] 4.6 Define `UserService` interface and implement `UserServiceImpl` in `com.easytickets.business.services`
    - Interface methods: `login(LoginRequest)`, `register(RegisterRequest, UserRole)`, `getMyProfile(String keycloakUserId)`, `updateMyProfile(String keycloakUserId, UpdateProfileRequest)`, `getTicketHistory(String keycloakUserId, int page, int size)`, `getOrganizerHistory(String keycloakUserId)`
    - `login`: POST to Keycloak token endpoint via `RestTemplate`, parse `access_token`, `refresh_token`, and roles from `resource_access.{clientId}.roles`; throw `UnauthorizedException(INVALID_CREDENTIALS)` on 401; throw `KeycloakUnavailableException` on timeout/5xx/unreachable; never log token values
    - `register`: Create Keycloak user via Admin Client; parse UUID from `Location` header; assign client role; build `UserProfileDto` with `id = keycloakUserId` and `createdBy = keycloakUserId` (set explicitly for self-service); save via `UserProfileRepo`; on DB failure delete Keycloak user and throw `RegistrationFailedException`; for admin registration let `AuditorAware` supply `createdBy`; throw `UserAlreadyExistsException` on Keycloak 409; log `INFO` on success with `userId` and `role`
    - `getMyProfile`: call `userProfileRepo.findActiveById(sub)`, throw `ProfileNotFoundException` if absent, map to `UserProfileResponse`
    - `updateMyProfile`: load active profile, apply only non-null fields from `UpdateProfileRequest`, save, return updated `UserProfileResponse`
    - `getTicketHistory`: call `OrderServiceClient`, pass `Authorization` header; wrap `FeignException` in `OrderServiceUnavailableException`
    - `getOrganizerHistory`: call `EventServiceClient`, pass `Authorization` header; wrap `FeignException` in `EventServiceUnavailableException`
    - _Requirements: 1.2, 1.3, 1.5, 1.6, 1.7, 2.2–2.8, 3.2–3.8, 4.2–4.10, 5.3–5.6, 6.3–6.11, 7.3–7.7, 8.3–8.7, 9.4, 9.5, 9.6, 14.1, 14.2, 14.4, 14.5_

  - [ ]* 4.7 Write property test for login validation rejection (Property 1)
    - **Property 1: Login Validation Rejection**
    - Generate blank strings, whitespace-only strings, and strings >255 chars for username and/or password; assert HTTP 400 with `VALIDATION_ERROR`; verify `RestTemplate.postForEntity` is never called
    - **Validates: Requirements 1.4, 1.8**

  - [ ]* 4.8 Write property test for login response mapping fidelity (Property 2)
    - **Property 2: Login Response Mapping Fidelity**
    - Generate random `accessToken`, `refreshToken`, and role lists; stub `RestTemplate` response; assert `LoginResponse` fields match generated values exactly
    - **Validates: Requirements 1.3**

  - [ ]* 4.9 Write property test for registration – Keycloak user and role assignment (Property 3)
    - **Property 3: Registration – Keycloak User Created with Correct Role**
    - Generate valid registration inputs (bounded strings per constraint) for all three roles; verify Admin Client `users().create()` is called and the matching client role is assigned before profile save
    - **Validates: Requirements 2.2, 2.3, 3.2, 3.3, 4.6, 4.7**

  - [ ]* 4.10 Write property test for registration – UserProfile id equals Keycloak UUID (Property 4)
    - **Property 4: Registration – UserProfile id Equals Keycloak UUID**
    - Generate random UUIDs as `Location` header value and random valid full names; assert `UserProfileDto.id` equals UUID and `fullName` matches submitted value
    - **Validates: Requirements 2.4, 3.4, 4.7, 9.3**

  - [ ]* 4.11 Write property test for registration validation – no Keycloak call for invalid input (Property 5)
    - **Property 5: Registration Validation – No Keycloak Call for Invalid Input**
    - Generate inputs violating each constraint boundary; assert HTTP 400 `VALIDATION_ERROR`; verify zero Admin Client invocations
    - **Validates: Requirements 2.6, 3.6, 4.5**

  - [ ]* 4.12 Write property test for registration rollback on DB failure (Property 6)
    - **Property 6: Registration Rollback on DB Failure**
    - Stub successful Keycloak user creation + any DB exception on `save()`; assert `keycloak.realm().users().delete(userId)` is called and HTTP 500 with `REGISTRATION_FAILED` is returned
    - **Validates: Requirements 2.8, 3.8, 4.10**

  - [ ]* 4.13 Write property test for self-service registration sets createdBy to own id (Property 11)
    - **Property 11: Self-Service Registration Sets createdBy to Own ID**
    - Generate buyer/organizer registration inputs; after save, assert `UserProfileDto.createdBy` equals `UserProfileDto.id`
    - **Validates: Requirements 9.5**

- [ ] 5. Checkpoint — business layer complete
  - Ensure all tests pass (including optional property tests if run), ask the user if questions arise.

- [ ] 6. Implement `UserService-infratructures` module
  - [ ] 6.1 Create `RecordStatus` enum and `BaseEntity` in `com.easytickets.infratructures.model`
    - `RecordStatus`: `ACTIVE`, `DELETED`
    - `BaseEntity`: `@MappedSuperclass @Data @EntityListeners(AuditingEntityListener.class)` with `@Id @GeneratedValue(strategy=UUID) String id`, `@Enumerated(STRING) RecordStatus deleteFlag = ACTIVE`, `@CreatedBy String createdBy`, `@CreatedDate LocalDateTime createdAt`, `@LastModifiedBy String updatedBy`, `@LastModifiedDate LocalDateTime updatedAt`
    - _Requirements: 9.2, 12.4_

  - [ ] 6.2 Create `UserProfile` entity in `com.easytickets.infratructures.model`
    - Extends `BaseEntity`; overrides `@Id @Column(name="id", length=36, nullable=false) String id` to suppress `@GeneratedValue`
    - `@Entity @Table(name="user_profiles") @Where(clause="delete_flag = 'ACTIVE'") @Data @EqualsAndHashCode(callSuper=true) @EntityListeners(AuditingEntityListener.class)`
    - Fields: `@Column(name="full_name", length=100, nullable=false) String fullName`, `@Column(length=20) String phone`, `@Column(name="avatar_url", length=255) String avatarUrl`, `@Column(length=255) String address`
    - _Requirements: 9.1, 9.2, 9.3, 9.7–9.10, 12.4_

  - [ ] 6.3 Create `JpaConfig` in `com.easytickets.infratructures.config`
    - `@Configuration @EnableJpaAuditing(auditorAwareRef="auditorProvider") @EnableJpaRepositories(basePackages={"com.easytickets"}) @EntityScan(basePackages={"com.easytickets"})`
    - `AuditorAware<String>` bean returning `jwt.getSubject()` from `SecurityContextHolder`; returns `Optional.empty()` when no auth context
    - _Requirements: 9.4, 9.5, 9.6_

  - [ ] 6.4 Create `UserProfileRepository` JPA interface and `UserProfileMapper` MapStruct mapper
    - `UserProfileRepository extends JpaRepository<UserProfile, String>` in `com.easytickets.infratructures.repo`
    - `UserProfileMapper`: `@Mapper(componentModel="spring")` with methods `toDto(UserProfile)`, `toEntity(UserProfileDto)`, and `@BeanMapping(nullValuePropertyMappingStrategy=IGNORE) updateEntityFromDto(UserProfileDto, @MappingTarget UserProfile)` for partial update
    - _Requirements: 6.3, 9.9_

  - [ ] 6.5 Create `UserProfileRepositoryImpl` adapter in `com.easytickets.infratructures.shared`
    - `@Repository @RequiredArgsConstructor` implements `UserProfileRepo`
    - `save`: `mapper.toEntity(dto)` → `jpaRepository.save()` → `mapper.toDto()`
    - `findActiveById`: `jpaRepository.findById(id).map(mapper::toDto)` (`@Where` filters DELETED transparently)
    - `update`: load entity by id (throw `ProfileNotFoundException` if absent), call `mapper.updateEntityFromDto(dto, entity)`, save and map back
    - _Requirements: 5.5, 6.3, 9.9, 9.10_

  - [ ]* 6.6 Write property test for profile lookup returns all active fields (Property 7)
    - **Property 7: Profile Lookup Returns All Active Fields**
    - Generate random `UserProfile` instances with `deleteFlag=ACTIVE`; call `findActiveById`; assert all five fields (`id`, `fullName`, `phone`, `avatarUrl`, `address`) match stored values; absent optional fields are `null`
    - **Validates: Requirements 5.3, 5.5**

  - [ ]* 6.7 Write property test for partial update preserves unchanged fields (Property 8)
    - **Property 8: Partial Update Preserves Unchanged Fields**
    - Generate random `UserProfile` and a random subset of update fields; call `updateMyProfile`; assert only provided non-null fields changed; `id`, `createdAt`, `createdBy`, `deleteFlag` unchanged
    - **Validates: Requirements 6.3, 6.11**

  - [ ]* 6.8 Write property test for update field validation rejects over-length values (Property 9)
    - **Property 9: Update Field Validation Rejects Over-Length Values**
    - Generate values exceeding max lengths per field (`fullName`>100, `phone`>20, `avatarUrl`>255, `address`>255); assert HTTP 400 `VALIDATION_ERROR`; profile unchanged
    - **Validates: Requirements 6.6, 6.7, 6.8, 6.9**

  - [ ]* 6.9 Write property test for soft delete exclusion – DELETED records never returned (Property 10)
    - **Property 10: Soft Delete Exclusion – DELETED Records Never Returned**
    - Persist `UserProfile` instances with `deleteFlag=DELETED`; call `findActiveById`; assert `Optional.empty()` is returned every time
    - **Validates: Requirements 9.7, 9.8, 9.9, 9.10**

- [ ] 7. Implement `UserService-application` module
  - [ ] 7.1 Create `Application.java` entry point in `com.easytickets.application`
    - `@SpringBootApplication @ComponentScan(basePackages={"com.easytickets"}) @EnableMethodSecurity @EnableFeignClients(basePackages="com.easytickets")`
    - `main` method calling `SpringApplication.run`
    - _Requirements: 12.1, 12.2_

  - [ ] 7.2 Create `SecurityConfig`, `SecurityProperties`, and `CustomJwtAuthenticationConverter` in `com.easytickets.application.config`
    - `SecurityProperties`: `@ConfigurationProperties(prefix="url")` holding `List<ApiPath> permit` (each `ApiPath` has `path` and `List<String> methods`)
    - `CustomJwtAuthenticationConverter`: reads `resource_access.{clientId}.roles` from JWT, prefixes each with `ROLE_`, returns `JwtAuthenticationToken`; `clientId` injected from `KeycloakConfigProperties`
    - `SecurityConfig`: disable CSRF; iterate permit list to call `permitAll()`; all other requests `.authenticated()`; OAuth2 Resource Server with `CustomJwtAuthenticationConverter`; configure `AuthenticationEntryPoint` and `AccessDeniedHandler` to return `ApiResponse.error(...)` JSON for 401/403
    - Permitted without token: `POST /api/v1/users/login`, `POST /api/v1/users/register/buyer`, `POST /api/v1/users/register/organizer`, `GET /actuator/health`
    - _Requirements: 11.1–11.7_

  - [ ] 7.3 Create `GlobalExceptionHandler` in `com.easytickets.application.exception`
    - `@RestControllerAdvice @Slf4j`
    - Handle `MethodArgumentNotValidException` → 400 `VALIDATION_ERROR` with per-field message list; populate `traceId` from MDC
    - Handle `BusinessException` → status and errorCode from exception
    - Handle `Exception` (fallback) → 500 `INTERNAL_SERVER_ERROR`; log full stack trace at ERROR; never include stack trace in response body
    - All responses set `traceId` from MDC key `traceId`; use empty string when MDC is empty
    - _Requirements: 10.3, 10.4, 10.5, 10.6, 10.7, 12.5_

  - [ ] 7.4 Create `UserController` in `com.easytickets.application.controller`
    - `@RestController @RequestMapping("api/v1/users")`, all methods return `ResponseEntity<ApiResponse<T>>`
    - `POST /login` — `@Valid LoginRequest` → delegate to `userService.login()` → 200
    - `POST /register/buyer` — `@Valid RegisterRequest` → `userService.register(req, BUYER)` → 201
    - `POST /register/organizer` — `@Valid RegisterRequest` → `userService.register(req, ORGANIZER)` → 201
    - `POST /register/admin` — `@PreAuthorize("hasRole('ADMIN')")` + `@Valid RegisterRequest` → `userService.register(req, ADMIN)` → 201
    - `GET /me` — extract `sub` from JWT principal; throw `UnauthorizedException(INVALID_TOKEN)` if blank; delegate to `userService.getMyProfile(sub)` → 200
    - `PUT /me` — extract `sub`; `@Valid UpdateProfileRequest` → `userService.updateMyProfile(sub, req)` → 200
    - `GET /me/ticket-history` — `@RequestParam(defaultValue="0") int page`, `@RequestParam(defaultValue="20") @Min(1) @Max(100) int size`; extract `Authorization` header; delegate to `userService.getTicketHistory(sub, page, size)` → 200
    - `GET /me/organizer-history` — `@PreAuthorize("hasRole('ORGANIZER')")` + extract `Authorization` header; delegate to `userService.getOrganizerHistory(sub)` → 200
    - _Requirements: 1.1, 2.1, 3.1, 4.1, 4.2–4.4, 5.1–5.6, 6.1–6.11, 7.1–7.7, 8.1–8.7_

  - [ ] 7.5 Create `application.yaml` and `logback-spring.xml` in `UserService-application/src/main/resources`
    - `application.yaml`: server port `${SERVER_PORT:8092}`, datasource MySQL, `liquibase.enabled=false`, OAuth2 resource server JWK URI, keycloak properties, `services.order-service.url`, `services.event-service.url`, Feign timeouts 5 000 ms, management/OTel/Prometheus endpoints, logging levels, `url.permit` list as defined in design
    - `logback-spring.xml`: `CONSOLE` appender with trace pattern, `LOGSTASH` `LogstashTcpSocketAppender` to `${LOGSTASH_HOST:-logstash}:${LOGSTASH_PORT:-5000}` with `LogstashEncoder` including MDC keys `traceId`/`spanId` and custom field `service:UserService-application`; root level INFO; `com.easytickets` logger DEBUG
    - _Requirements: 11.1, 11.2, 13.3, 14.4_

  - [ ]* 7.6 Write property test for downstream pass-through fidelity (Property 12)
    - **Property 12: Downstream Pass-Through Fidelity**
    - Generate random Order/Event Service response payloads; stub Feign clients; assert aggregation response `data` equals the downstream `data` including empty-list/empty-object cases
    - **Validates: Requirements 7.4, 7.5, 8.5, 8.6**

- [ ] 8. Implement `UserService-worker` module
  - [ ] 8.1 Create placeholder `WorkerApplication.java` in `com.easytickets.worker`
    - Create minimal Spring Boot entry point to maintain structural conformance; no Kafka consumers or producers are required for the current feature set
    - _Requirements: 12.1_

- [ ] 9. Wire all modules together and validate the full build
  - [ ] 9.1 Verify module dependency enforcement
    - Confirm `UserService-business` POM has no dependency on `UserService-infratructures`; confirm `UserService-infratructures` depends on `UserService-business`; confirm `UserService-application` depends on both `business` and `infratructures`
    - Run `mvn validate` or `mvn compile` from the root to confirm build succeeds and no circular dependency violations exist
    - _Requirements: 12.3, 12.6_

  - [ ] 9.2 Validate component scan and JPA auditing wiring
    - Confirm `@ComponentScan(basePackages={"com.easytickets"})` on `Application.java` picks up beans from all modules
    - Confirm `@EnableJpaRepositories` and `@EntityScan` in `JpaConfig` cover `com.easytickets`
    - Confirm `@EnableFeignClients` scans `com.easytickets` to register both Feign clients
    - _Requirements: 12.2_

- [ ] 10. Final checkpoint — Ensure all tests pass, ask the user if questions arise.

---

## Notes

- Tasks marked with `*` are optional and can be skipped for a faster MVP; remove the `*` prefix when you want them executed.
- Each property test requires `net.jqwik:jqwik:1.8.x` added to the relevant module's test dependencies; annotate each test method with `// Feature: user-service, Property N: <title>` and `@Property(tries = 100)`.
- The design document has no test generation during implementation (per `tech.md` §13 and `structure.md` §6); all `src/test/` files belong to the optional property-test tasks only.
- Checkpoints at tasks 5 and 10 are manual validation gates — the implementation agent should pause and surface any questions before proceeding.
- `UserProfile.id` overrides `BaseEntity.id` to suppress `@GeneratedValue` — the Keycloak UUID from the `Location` header is always set explicitly before `save()`.
- For self-service (buyer/organizer) registration, `createdBy` must be set manually to the Keycloak UUID because `AuditorAware` returns `Optional.empty()` with no auth context.
- All error codes are centralised in `AppConstants` in the `common` module to avoid magic strings across modules.

---

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1"] },
    { "id": 1, "tasks": ["2.1", "2.2"] },
    { "id": 2, "tasks": ["3.1", "3.2", "4.1", "4.2"] },
    { "id": 3, "tasks": ["4.3", "4.4", "4.5"] },
    { "id": 4, "tasks": ["4.6"] },
    { "id": 5, "tasks": ["4.7", "4.8", "4.9", "4.10", "4.11", "4.12", "4.13", "6.1"] },
    { "id": 6, "tasks": ["6.2", "6.3"] },
    { "id": 7, "tasks": ["6.4"] },
    { "id": 8, "tasks": ["6.5"] },
    { "id": 9, "tasks": ["6.6", "6.7", "6.8", "6.9", "7.1"] },
    { "id": 10, "tasks": ["7.2", "7.3"] },
    { "id": 11, "tasks": ["7.4", "7.5"] },
    { "id": 12, "tasks": ["7.6", "8.1"] },
    { "id": 13, "tasks": ["9.1", "9.2"] }
  ]
}
```
