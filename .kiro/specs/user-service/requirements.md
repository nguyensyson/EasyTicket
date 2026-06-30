# Requirements Document

## Introduction

UserService is a new microservice in the EasyTicket platform. It acts as the intermediary between client applications and Keycloak (identity provider) and owns the internal user profile data store. UserService is responsible for authentication delegation (login), self-service and administrative registration, profile management, and aggregating user-centric data from other services (purchased ticket history from Order Service, hosted-event statistics from Event Service).

UserService uses **MySQL** as its relational database, consistent with the JDBC driver and column-type conventions in `tech.md`.

UserService MUST conform to the EasyTicket engineering standards defined in the steering documents (`product.md`, `tech.md`, `structure.md`):

- Maven multi-module layout: `UserService-application`, `UserService-business`, `UserService-common`, `UserService-infratructures`, `UserService-migration`, `UserService-worker`.
- Base package `com.easytickets` with per-module sub-packages.
- OAuth2 Resource Server validating Keycloak-issued JWTs, with role extraction from `resource_access.{client-id}.roles`.
- Standard `ApiResponse<T>` wrapper for all HTTP responses.
- `BusinessException` hierarchy plus a global `@RestControllerAdvice` exception handler.
- `BaseEntity` providing audit fields (`created_at`, `created_by`, `updated_at`, `updated_by`) and soft delete (`delete_flag`), with Spring Data JPA Auditing.
- Liquibase schema migrations isolated in the `UserService-migration` module, with changelog under `src/main/resources/db/changelog`.

This document defines WHAT UserService must do. Implementation detail (HOW) is deferred to the design phase.

## Glossary

- **UserService**: The microservice specified by this document; the deployable EasyTicket user management service.
- **Auth_Endpoint**: The component of UserService that handles `POST /api/v1/users/login` and delegates authentication to Keycloak.
- **Registration_Service**: The component of UserService that handles buyer, organizer, and admin registration endpoints.
- **Profile_Service**: The component of UserService that handles read and update of the authenticated user's profile (`GET /api/v1/users/me`, `PUT /api/v1/users/me`).
- **Aggregation_Service**: The component of UserService that retrieves cross-service data (ticket history, organizer history).
- **Keycloak_Client**: The component of UserService that communicates with Keycloak using the Keycloak Admin Client and the OpenID Connect token endpoint.
- **Order_Service_Client**: The synchronous HTTP client (OpenFeign) used by UserService to call Order Service.
- **Event_Service_Client**: The synchronous HTTP client (OpenFeign) used by UserService to call Event Service.
- **Security_Filter**: The OAuth2 Resource Server security filter chain that validates JWT access tokens.
- **User_Profile**: A record in the `user_profiles` table representing the internal profile of a single user. The `id` equals the Keycloak user UUID.
- **Keycloak**: The external identity and access management server that issues tokens and stores credentials.
- **Order_Service**: The external EasyTicket service that owns purchased-ticket and order data.
- **Event_Service**: The external EasyTicket service that owns event catalog and event statistics data.
- **Access_Token**: A short-lived JWT issued by Keycloak used to authorize API calls.
- **Refresh_Token**: A token issued by Keycloak used to obtain a new Access_Token.
- **Role**: A Keycloak authorization role; valid values for this service are BUYER, ORGANIZER, ADMIN.
- **ApiResponse**: The standard response wrapper `{ success, errorCode, message, data, traceId }`.
- **BusinessException**: The base runtime exception type carrying an `errorCode` and an HTTP status.
- **BaseEntity**: The abstract JPA superclass providing audit fields and the `delete_flag` soft-delete column.
- **RecordStatus**: The enum with values ACTIVE and DELETED used by `delete_flag`.
- **Authenticated_Admin**: A caller whose validated JWT carries the ADMIN role.

> **Registration field constraints (applies to Requirements 2, 3, and 4):** username is 3–50 characters; password is 8–128 characters; email is 1–255 characters and matches a valid `local-part@domain` email format; full name is 1–100 characters. "Valid required fields" in the registration requirements means all of these constraints are satisfied.

## Requirements

### Requirement 1: User Login (Authentication Delegation)

**User Story:** As a registered user, I want to log in with my username and password, so that I receive tokens and my roles to access the platform.

#### Acceptance Criteria

1. THE Auth_Endpoint SHALL expose `POST /api/v1/users/login` accepting a JSON body containing a username and a password.
2. WHEN a login request is received with a username of 1–255 non-whitespace characters and a password of 1–255 characters, THE Auth_Endpoint SHALL request a token from Keycloak using the OpenID Connect password grant with a connection-and-read timeout of 5 seconds.
3. WHEN Keycloak returns a successful token response within 5 seconds, THE Auth_Endpoint SHALL return, within 2 seconds of receiving that response, an ApiResponse with HTTP status 200 and `success` equal to true containing the Access_Token, the Refresh_Token, and the list of Role values extracted from the Keycloak token.
4. IF the username or the password is missing, empty, or whitespace-only, THEN THE Auth_Endpoint SHALL return an ApiResponse with HTTP status 400 and errorCode `VALIDATION_ERROR` and SHALL NOT send a token request to Keycloak.
5. IF Keycloak rejects the credentials, THEN THE Auth_Endpoint SHALL return an ApiResponse with HTTP status 401 and errorCode `INVALID_CREDENTIALS` that contains no Access_Token and no Refresh_Token.
6. IF the Keycloak token request is unreachable, exceeds the 5-second timeout, or returns a server error, THEN THE Auth_Endpoint SHALL return an ApiResponse with HTTP status 502 and errorCode `KEYCLOAK_UNAVAILABLE`.
7. THE Auth_Endpoint SHALL exclude the password, the Access_Token, and the Refresh_Token values from all log output.
8. IF the username exceeds 255 characters or the password exceeds 255 characters, THEN THE Auth_Endpoint SHALL return an ApiResponse with HTTP status 400 and errorCode `VALIDATION_ERROR`.

### Requirement 2: Buyer Registration

**User Story:** As a prospective ticket buyer, I want to register an account, so that I can purchase tickets on EasyTicket.

#### Acceptance Criteria

1. THE Registration_Service SHALL expose `POST /api/v1/users/register/buyer` accepting a registration request containing a username, a password, an email, and a full name, subject to the registration field constraints defined in the Glossary.
2. WHEN a buyer registration request is received with all required fields satisfying the registration field constraints, THE Keycloak_Client SHALL create the corresponding user in Keycloak.
3. WHEN the Keycloak user is created for a buyer, THE Registration_Service SHALL assign the Role BUYER to that user in Keycloak.
4. WHEN the Keycloak user is created and the BUYER role is assigned, THE Registration_Service SHALL persist a User_Profile whose `id` equals the Keycloak user UUID and whose `full_name` equals the submitted full name.
5. WHEN buyer registration completes, THE Registration_Service SHALL return an ApiResponse with HTTP status 201 and `success` equal to true whose `data` contains the created User_Profile identifier.
6. IF a required field is missing or fails the registration field constraints, THEN THE Registration_Service SHALL return an ApiResponse with HTTP status 400, `success` equal to false, and errorCode `VALIDATION_ERROR`, and SHALL NOT create a Keycloak user or persist a User_Profile.
7. IF the requested username or email already exists in Keycloak, THEN THE Registration_Service SHALL return an ApiResponse with HTTP status 409, `success` equal to false, and errorCode `USER_ALREADY_EXISTS`, and SHALL NOT persist a User_Profile.
8. IF the Keycloak user is created but the subsequent User_Profile persistence fails, THEN THE Registration_Service SHALL remove or disable the created Keycloak user so that no partially-active account remains usable, and SHALL return an ApiResponse with HTTP status 500, `success` equal to false, and errorCode `REGISTRATION_FAILED`.
9. WHERE no Access_Token is supplied, THE Registration_Service SHALL allow `POST /api/v1/users/register/buyer` to be processed.

### Requirement 3: Organizer Registration

**User Story:** As a prospective event organizer, I want to register an organizer account, so that I can create and manage events on EasyTicket.

#### Acceptance Criteria

1. THE Registration_Service SHALL expose `POST /api/v1/users/register/organizer` accepting a registration request containing a username, a password, an email, and a full name, subject to the registration field constraints defined in the Glossary.
2. WHEN an organizer registration request is received with all required fields satisfying the registration field constraints, THE Keycloak_Client SHALL create the corresponding user in Keycloak.
3. WHEN the Keycloak user is created for an organizer, THE Registration_Service SHALL assign the Role ORGANIZER to that user in Keycloak.
4. WHEN the Keycloak user is created and the ORGANIZER role is assigned, THE Registration_Service SHALL persist a User_Profile whose `id` equals the Keycloak user UUID and whose `full_name` equals the submitted full name.
5. WHEN organizer registration completes, THE Registration_Service SHALL return an ApiResponse with HTTP status 201 and `success` equal to true whose `data` contains the created User_Profile identifier.
6. IF a required field is missing or fails the registration field constraints, THEN THE Registration_Service SHALL return an ApiResponse with HTTP status 400, `success` equal to false, and errorCode `VALIDATION_ERROR`, and SHALL NOT create a Keycloak user or persist a User_Profile.
7. IF the requested username or email already exists in Keycloak, THEN THE Registration_Service SHALL return an ApiResponse with HTTP status 409, `success` equal to false, and errorCode `USER_ALREADY_EXISTS`, and SHALL NOT persist a User_Profile.
8. IF the Keycloak user is created but the subsequent User_Profile persistence fails, THEN THE Registration_Service SHALL remove or disable the created Keycloak user so that no partially-active account remains usable, and SHALL return an ApiResponse with HTTP status 500, `success` equal to false, and errorCode `REGISTRATION_FAILED`.
9. WHERE no Access_Token is supplied, THE Registration_Service SHALL allow `POST /api/v1/users/register/organizer` to be processed.

### Requirement 4: Admin Registration

**User Story:** As a system administrator, I want to create admin accounts, so that additional administrators can manage the platform.

#### Acceptance Criteria

1. THE Registration_Service SHALL expose `POST /api/v1/users/register/admin` accepting a registration request containing a username, a password, an email, and a full name, subject to the registration field constraints defined in the Glossary.
2. WHILE the caller's validated JWT carries the ADMIN role, THE Registration_Service SHALL process the admin registration request.
3. IF the request has no valid Access_Token, THEN THE Security_Filter SHALL reject the request with HTTP status 401.
4. IF the caller is authenticated but the validated JWT does not carry the ADMIN role, THEN THE Registration_Service SHALL reject the request with HTTP status 403 and errorCode `FORBIDDEN`.
5. IF the request is from an Authenticated_Admin but a required field is missing or fails the registration field constraints, THEN THE Registration_Service SHALL return an ApiResponse with HTTP status 400, `success` equal to false, and errorCode `VALIDATION_ERROR`, and SHALL NOT create a Keycloak user or persist a User_Profile.
6. WHEN an admin registration request is received from an Authenticated_Admin with all required fields satisfying the registration field constraints, THE Keycloak_Client SHALL create the corresponding user in Keycloak.
7. WHEN the Keycloak user is created for an admin, THE Registration_Service SHALL assign the Role ADMIN to that user and SHALL persist a User_Profile whose `id` equals the Keycloak user UUID and whose `full_name` equals the submitted full name.
8. WHEN admin registration completes, THE Registration_Service SHALL return an ApiResponse with HTTP status 201 and `success` equal to true whose `data` contains the created User_Profile identifier.
9. IF the requested username or email already exists in Keycloak, THEN THE Registration_Service SHALL return an ApiResponse with HTTP status 409, `success` equal to false, and errorCode `USER_ALREADY_EXISTS`, and SHALL NOT persist a User_Profile.
10. IF the Keycloak user is created but the subsequent User_Profile persistence fails, THEN THE Registration_Service SHALL remove or disable the created Keycloak user so that no partially-active account remains usable, and SHALL return an ApiResponse with HTTP status 500, `success` equal to false, and errorCode `REGISTRATION_FAILED`.

### Requirement 5: Retrieve Current User Profile

**User Story:** As an authenticated user, I want to retrieve my own profile, so that I can view my personal information.

#### Acceptance Criteria

1. THE Profile_Service SHALL expose `GET /api/v1/users/me` requiring a valid Access_Token.
2. IF the request has a missing, expired, or signature/issuer-invalid Access_Token, THEN THE Security_Filter SHALL reject the request with HTTP status 401 and SHALL return no User_Profile data.
3. WHEN an authenticated request is received, THE Profile_Service SHALL resolve the caller identity from the JWT `sub` claim.
4. IF the validated JWT lacks a non-empty `sub` claim, THEN THE Profile_Service SHALL return an ApiResponse with HTTP status 401 and errorCode `INVALID_TOKEN`.
5. WHEN a User_Profile with `id` equal to the JWT `sub` claim exists and has `delete_flag` equal to ACTIVE, THE Profile_Service SHALL return, within 2000 ms, an ApiResponse with HTTP status 200 and `success` equal to true containing that User_Profile's `id`, `full_name`, `phone`, `avatar_url`, and `address`, where any absent optional field (`phone`, `avatar_url`, `address`) is represented as null.
6. IF no User_Profile with `id` equal to the JWT `sub` claim and `delete_flag` equal to ACTIVE exists, THEN THE Profile_Service SHALL return an ApiResponse with HTTP status 404, `success` equal to false, `data` equal to null, and errorCode `PROFILE_NOT_FOUND`.

### Requirement 6: Update Current User Profile

**User Story:** As an authenticated user, I want to update my personal information, so that my profile stays accurate.

#### Acceptance Criteria

1. THE Profile_Service SHALL expose `PUT /api/v1/users/me` requiring a valid Access_Token.
2. IF the request has no valid Access_Token, THEN THE Security_Filter SHALL reject the request with HTTP status 401.
3. WHEN an authenticated update request is received with valid fields, THE Profile_Service SHALL update only the fields present in the request body among `full_name`, `phone`, `avatar_url`, and `address` of the User_Profile whose `id` equals the JWT `sub` claim, and SHALL leave fields omitted from the body unchanged.
4. WHEN the update completes, THE Profile_Service SHALL set `updated_at` and `updated_by` through Spring Data JPA Auditing using the JWT `sub` claim as `updated_by`.
5. WHEN the update completes, THE Profile_Service SHALL return an ApiResponse with HTTP status 200 and `success` equal to true containing the updated User_Profile.
6. IF the `phone` field is present and exceeds 20 characters, THEN THE Profile_Service SHALL return an ApiResponse with HTTP status 400 and errorCode `VALIDATION_ERROR` and SHALL NOT modify the User_Profile.
7. IF the `full_name` field is present and is empty or exceeds 100 characters, THEN THE Profile_Service SHALL return an ApiResponse with HTTP status 400 and errorCode `VALIDATION_ERROR` and SHALL NOT modify the User_Profile.
8. IF the `avatar_url` field is present and exceeds 255 characters, THEN THE Profile_Service SHALL return an ApiResponse with HTTP status 400 and errorCode `VALIDATION_ERROR` and SHALL NOT modify the User_Profile.
9. IF the `address` field is present and exceeds 255 characters, THEN THE Profile_Service SHALL return an ApiResponse with HTTP status 400 and errorCode `VALIDATION_ERROR` and SHALL NOT modify the User_Profile.
10. IF no User_Profile with `id` equal to the JWT `sub` claim and `delete_flag` equal to ACTIVE exists, THEN THE Profile_Service SHALL return an ApiResponse with HTTP status 404 and errorCode `PROFILE_NOT_FOUND`.
11. THE Profile_Service SHALL reject attempts to modify `id`, `created_at`, `created_by`, or `delete_flag` through this endpoint by ignoring those fields in the request body.

### Requirement 7: Retrieve Ticket Purchase History

**User Story:** As an authenticated buyer, I want to view my purchased ticket history, so that I can review tickets I have bought.

#### Acceptance Criteria

1. THE Aggregation_Service SHALL expose `GET /api/v1/users/me/ticket-history` requiring a valid Access_Token and accepting optional pagination parameters `page` (default 0) and `size` (default 20, minimum 1, maximum 100).
2. IF the request has a missing, malformed, or expired Access_Token, THEN THE Security_Filter SHALL reject the request with HTTP status 401 and THE Order_Service_Client SHALL NOT invoke Order_Service.
3. WHEN an authenticated request is received, THE Order_Service_Client SHALL call Order_Service to retrieve the tickets purchased by the user identified by the JWT `sub` claim, forwarding the caller's Access_Token in the Authorization header, with a request timeout of 5 seconds.
4. WHEN Order_Service returns a successful response, THE Aggregation_Service SHALL return an ApiResponse with HTTP status 200 and `success` equal to true containing the list of purchased tickets returned by Order_Service.
5. WHEN Order_Service returns an empty result for the user, THE Aggregation_Service SHALL return an ApiResponse with HTTP status 200 and `success` equal to true containing an empty list.
6. IF Order_Service is unreachable, returns a 5xx server error, or exceeds the 5-second request timeout, THEN THE Aggregation_Service SHALL return an ApiResponse with HTTP status 502 and errorCode `ORDER_SERVICE_UNAVAILABLE` and SHALL return no ticket data.
7. IF Order_Service returns a non-401 4xx client error, THEN THE Aggregation_Service SHALL return an ApiResponse with HTTP status 502 and errorCode `ORDER_SERVICE_UNAVAILABLE` and SHALL return no ticket data.

### Requirement 8: Retrieve Organizer Event Statistics

**User Story:** As an authenticated organizer, I want to view statistics of events I have hosted, so that I can track my events' performance.

#### Acceptance Criteria

1. THE Aggregation_Service SHALL expose `GET /api/v1/users/me/organizer-history` requiring a valid Access_Token.
2. IF the request has a missing, malformed, or expired Access_Token, THEN THE Security_Filter SHALL reject the request with HTTP status 401 and THE Event_Service_Client SHALL NOT invoke Event_Service.
3. IF the caller is authenticated but the validated JWT does not carry the ORGANIZER role, THEN THE Aggregation_Service SHALL reject the request with HTTP status 403 and errorCode `FORBIDDEN` and SHALL NOT call Event_Service.
4. WHEN an authenticated organizer request is received, THE Event_Service_Client SHALL call Event_Service to retrieve the event statistics for the organizer identified by the JWT `sub` claim, forwarding the caller's Access_Token in the Authorization header.
5. WHEN Event_Service returns a successful response, THE Aggregation_Service SHALL return an ApiResponse with HTTP status 200 and `success` equal to true containing the event statistics returned by Event_Service.
6. WHEN Event_Service returns a successful response indicating the organizer has hosted zero events, THE Aggregation_Service SHALL return an ApiResponse with HTTP status 200 and `success` equal to true containing empty statistics.
7. IF Event_Service is unreachable, returns a 5xx server error, or exceeds a 5-second request timeout, THEN THE Aggregation_Service SHALL return an ApiResponse with HTTP status 502 and errorCode `EVENT_SERVICE_UNAVAILABLE` and SHALL return no statistics data.

### Requirement 9: User Profile Persistence and Soft Delete

**User Story:** As the platform operator, I want user profiles stored consistently with audit and soft-delete semantics, so that data is traceable and never physically lost.

#### Acceptance Criteria

1. THE UserService SHALL persist each User_Profile in the `user_profiles` table with columns `id` (VARCHAR(36) primary key), `full_name` (VARCHAR(100) NOT NULL), `phone` (VARCHAR(20)), `avatar_url` (VARCHAR(255)), and `address` (VARCHAR(255)).
2. THE UserService SHALL store each User_Profile with audit columns `created_at`, `created_by` (VARCHAR(255)), `updated_at`, `updated_by` (VARCHAR(255)), and a `delete_flag` column of type RecordStatus that is NOT NULL and defaults to ACTIVE.
3. THE UserService SHALL set the User_Profile `id` equal to the Keycloak user UUID rather than generating a separate identifier.
4. WHEN a User_Profile is created within an authenticated context, THE UserService SHALL populate `created_at` and `created_by` through Spring Data JPA Auditing, using the JWT `sub` claim as `created_by`.
5. WHEN a User_Profile is created without an authenticated context (for example, self-service registration), THE UserService SHALL populate `created_at` and SHALL set `created_by` to the created User_Profile's own `id` (the Keycloak user UUID).
6. WHEN a User_Profile is updated, THE UserService SHALL populate `updated_at` and `updated_by` through Spring Data JPA Auditing, using the JWT `sub` claim as `updated_by`.
7. WHEN a profile deletion is requested for an existing ACTIVE User_Profile, THE UserService SHALL set `delete_flag` to DELETED and SHALL retain the row rather than performing a physical delete.
8. IF a profile deletion is requested for a User_Profile that does not exist or already has `delete_flag` equal to DELETED, THEN THE UserService SHALL report errorCode `PROFILE_NOT_FOUND` and SHALL leave existing data unchanged.
9. THE UserService SHALL exclude User_Profile records with `delete_flag` equal to DELETED from all profile read operations, including both list reads and single-record reads.
10. WHEN a single-record read targets a User_Profile whose `delete_flag` equals DELETED, THE UserService SHALL treat the record as not found.

### Requirement 10: Standard Response and Error Handling

**User Story:** As a client developer, I want every response to follow the standard envelope, so that I can handle success and errors uniformly.

#### Acceptance Criteria

1. THE UserService SHALL return every HTTP response body as an ApiResponse containing the fields `success` (boolean), `errorCode` (string or null), `message` (string), `data` (object or null), and `traceId` (string).
2. WHEN a request succeeds, THE UserService SHALL return an ApiResponse with `success` equal to true, `errorCode` equal to null, and the result in `data`.
3. WHEN a BusinessException is raised, THE UserService SHALL return an ApiResponse with `success` equal to false, `data` equal to null, the exception's `errorCode`, and the HTTP status carried by the exception.
4. WHEN Bean Validation fails on a request body, THE UserService SHALL return an ApiResponse with HTTP status 400, `success` equal to false, `data` equal to null, errorCode `VALIDATION_ERROR`, and a `message` listing each rejected field with its validation error.
5. IF an unhandled exception occurs, THEN THE UserService SHALL return an ApiResponse with HTTP status 500, `success` equal to false, `data` equal to null, errorCode `INTERNAL_SERVER_ERROR`, and a non-null `traceId`, and SHALL exclude any stack trace, exception class name, and internal diagnostic detail from the response body.
6. WHEN constructing an ApiResponse while an active tracing context exists, THE UserService SHALL populate the `traceId` field from that tracing context.
7. IF no active tracing context exists when constructing an ApiResponse, THEN THE UserService SHALL set the `traceId` field to an empty string rather than null.

### Requirement 11: Security and Access Control

**User Story:** As the platform operator, I want endpoints protected by Keycloak-issued JWTs, so that only authorized callers reach protected resources.

#### Acceptance Criteria

1. THE UserService SHALL operate as an OAuth2 Resource Server that validates JWT Access_Tokens issued by Keycloak, checking signature, expiration, not-before, and that the issuer matches the configured Keycloak realm.
2. THE Security_Filter SHALL permit `POST /api/v1/users/login`, `POST /api/v1/users/register/buyer`, `POST /api/v1/users/register/organizer`, and `GET /actuator/health` without an Access_Token.
3. WHEN a request targets an endpoint not listed in the permit configuration, THE Security_Filter SHALL require a valid Access_Token.
4. WHEN a JWT is validated, THE Security_Filter SHALL extract Role values from the Keycloak claim `resource_access.{client-id}.roles` and SHALL expose each Role as a Spring Security authority prefixed with `ROLE_`.
5. WHEN a validated JWT has a missing or empty `resource_access.{client-id}.roles` claim, THE Security_Filter SHALL grant zero role authorities to the caller.
6. IF a request to a non-permitted endpoint has no Access_Token, an expired Access_Token, a malformed Access_Token, or an Access_Token that fails signature validation, THEN THE Security_Filter SHALL reject the request with HTTP status 401 and SHALL NOT invoke the protected resource.
7. IF an authenticated caller lacks a Role required by a protected endpoint, THEN THE UserService SHALL reject the request with HTTP status 403 and SHALL NOT invoke the protected resource.

### Requirement 12: Project Structure and Build Conformance

**User Story:** As an EasyTicket engineer, I want UserService to match the platform's standard structure, so that it is consistent and maintainable.

#### Acceptance Criteria

1. THE UserService SHALL be a Maven multi-module project whose parent POM declares exactly the six modules `UserService-application`, `UserService-business`, `UserService-common`, `UserService-infratructures`, `UserService-migration`, and `UserService-worker`.
2. THE UserService SHALL place all Java sources under the base package `com.easytickets`, with each module using its sub-package: application in `com.easytickets.application`, business in `com.easytickets.business`, common in `com.easytickets.common`, infratructures in `com.easytickets.infratructures`, and worker in `com.easytickets.worker`.
3. THE UserService business module SHALL define repository access through port interfaces, and the `UserService-business` POM SHALL NOT declare a dependency on the `UserService-infratructures` module; the infratructures module SHALL provide the port implementations.
4. THE UserService SHALL define a `BaseEntity` superclass in the infratructures module providing the fields `id`, `delete_flag`, `created_by`, `created_at`, `updated_by`, and `updated_at`, and the User_Profile entity SHALL extend it.
5. THE UserService SHALL define a `BusinessException` base type extending `RuntimeException` and a single global `@RestControllerAdvice` handler in the application module that returns a structured error response (errorCode and message) without any stack trace.
6. IF a module's source declares a dependency that violates the dependency direction (business depending on infratructures), THEN the Maven build SHALL fail.

### Requirement 13: Database Migration Management

**User Story:** As an EasyTicket engineer, I want schema changes managed by Liquibase in an isolated module, so that migrations run independently of the application.

#### Acceptance Criteria

1. THE UserService-migration module SHALL contain the Liquibase master changelog at `src/main/resources/db/changelog/changelog.xml`, and each change set entry in the master file SHALL reference an external migration SQL file rather than embedding SQL statements directly in the master file.
2. THE UserService-migration module SHALL include a change set that creates the `user_profiles` table with the columns and the six mandatory audit fields (`id`, `delete_flag`, `created_by`, `created_at`, `updated_by`, `updated_at`) specified in Requirement 9.
3. THE UserService-application module SHALL run with Liquibase disabled and SHALL NOT apply any change set at startup.
4. WHEN the UserService-migration module starts, THE UserService-migration module SHALL apply all change sets not yet recorded in the Liquibase tracking table, and SHALL leave already-recorded change sets unchanged.
5. IF a change set fails to apply during startup, THEN THE UserService-migration module SHALL stop applying further change sets, SHALL leave the remaining unapplied change sets unrecorded, and SHALL terminate with a non-zero exit status indicating the migration failure.
6. THE UserService-migration module SHALL name each migration SQL file using the pattern `V{number}_{YYYYMMDDHHmm}_{description}.sql` and SHALL use a change set id matching the file name without the `.sql` suffix.

### Requirement 14: Observability and Audit Logging

**User Story:** As an operator, I want meaningful logs and traces, so that I can monitor and troubleshoot the service.

#### Acceptance Criteria

1. WHEN a user registration completes successfully, THE UserService SHALL emit exactly one INFO-level log entry containing the created user identifier, the assigned Role, a timestamp, and the active traceId.
2. IF a login attempt is rejected by Keycloak, THEN THE UserService SHALL emit a WARN-level log entry that includes the rejection reason and excludes the password, the Access_Token, and the Refresh_Token.
3. IF an integration call to Order_Service or Event_Service fails with a non-2xx response, a connection error, or a timeout, THEN THE UserService SHALL emit an ERROR-level log entry containing the target service name and the failure status (HTTP status code or error category).
4. WHEN Order_Service_Client or Event_Service_Client makes an outbound call, THE UserService SHALL propagate the W3C trace context via the `traceparent` header.
5. THE UserService SHALL exclude passwords, Access_Tokens, and Refresh_Tokens from every log entry it emits, at all log levels.

### Requirement 15: Change History Recording

**User Story:** As an EasyTicket engineer, I want the work on this service recorded in the project changelog, so that the history of significant changes is preserved.

#### Acceptance Criteria

1. WHEN a change to UserService that is classified as one of the changelog's defined change types (FEATURE, BUGFIX, REFACTOR, CONFIG, MIGRATION, DEPENDENCY, SECURITY, or DOCS) is completed, THE engineering process SHALL add exactly one entry to `.kiro/steering/changelog.md` using the entry format defined in that file.
2. WHEN a changelog entry is added, THE changelog entry SHALL record the completion date and time in the format defined by the changelog file, exactly one change type selected from the defined set of change types, a non-empty description, and a list naming every affected file or module.
3. WHEN a new changelog entry is added, THE changelog process SHALL insert the entry above all existing entries and SHALL leave every prior entry unchanged.
4. IF `.kiro/steering/changelog.md` does not exist or the new entry does not conform to the file's defined entry format, THEN THE engineering process SHALL report an error indicating the changelog update failed and SHALL leave all existing entries unchanged.
