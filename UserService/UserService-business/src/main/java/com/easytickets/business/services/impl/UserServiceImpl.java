package com.easytickets.business.services.impl;

import com.easytickets.business.config.KeycloakConfigProperties;
import com.easytickets.business.dto.KeycloakTokenResponse;
import com.easytickets.business.dto.LoginRequest;
import com.easytickets.business.dto.LoginResponse;
import com.easytickets.business.dto.RegisterRequest;
import com.easytickets.business.dto.RegisterResponse;
import com.easytickets.business.dto.UserProfileDto;
import com.easytickets.business.dto.UserRole;
import com.easytickets.business.exception.InvalidCredentialsException;
import com.easytickets.business.exception.KeycloakUnavailableException;
import com.easytickets.business.exception.RegistrationFailedException;
import com.easytickets.business.exception.UserAlreadyExistsException;
import com.easytickets.business.repo.UserProfileRepo;
import com.easytickets.business.services.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final Keycloak keycloak;
    private final KeycloakConfigProperties keycloakProperties;
    private final UserProfileRepo userProfileRepo;
    private final RestTemplate keycloakRestTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public RegisterResponse register(RegisterRequest request, UserRole role) {
        String realm = keycloakProperties.getRealm();
        String keycloakUserId = createKeycloakUser(realm, request);

        try {
            assignRole(realm, keycloakUserId, role);

            UserProfileDto profile = UserProfileDto.builder()
                    .id(keycloakUserId)
                    .fullName(request.getFullName())
                    .createdBy(keycloakUserId)
                    .build();
            userProfileRepo.save(profile);
        } catch (Exception ex) {
            log.error("Registration failed after Keycloak user creation, rolling back. userId={}", keycloakUserId, ex);
            rollbackKeycloakUser(realm, keycloakUserId);
            throw new RegistrationFailedException("Registration failed while finalizing user account");
        }

        log.info("User registered successfully. userId={}, role={}", keycloakUserId, role);
        return RegisterResponse.builder().id(keycloakUserId).build();
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String tokenUrl = keycloakProperties.getServerUrl() + "/realms/" + keycloakProperties.getRealm()
                + "/protocol/openid-connect/token";

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", keycloakProperties.getClientId());
        form.add("client_secret", keycloakProperties.getClientSecret());
        form.add("username", request.getUsername());
        form.add("password", request.getPassword());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        KeycloakTokenResponse tokenResponse;
        try {
            tokenResponse = keycloakRestTemplate
                    .postForEntity(tokenUrl, new HttpEntity<>(form, headers), KeycloakTokenResponse.class)
                    .getBody();
        } catch (HttpClientErrorException.Unauthorized ex) {
            log.warn("Login failed, invalid credentials. username={}", request.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        } catch (HttpStatusCodeException ex) {
            log.error("Keycloak returned an unexpected status during login. username={}, status={}",
                    request.getUsername(), ex.getStatusCode(), ex);
            throw new KeycloakUnavailableException("Keycloak returned an unexpected error", ex);
        } catch (RestClientException ex) {
            log.error("Keycloak is unreachable during login. username={}", request.getUsername(), ex);
            throw new KeycloakUnavailableException("Keycloak is unavailable", ex);
        }

        if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
            log.error("Keycloak returned an empty token response. username={}", request.getUsername());
            throw new KeycloakUnavailableException("Keycloak returned an empty token response");
        }

        List<String> roles = extractRoles(tokenResponse.getAccessToken());
        log.info("User logged in successfully. username={}", request.getUsername());

        return LoginResponse.builder()
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .roles(roles)
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRoles(String accessToken) {
        try {
            String[] parts = accessToken.split("\\.");
            if (parts.length < 2) {
                return List.of();
            }
            String payload = parts[1];
            int padding = (4 - payload.length() % 4) % 4;
            payload = payload + "=".repeat(padding);
            byte[] payloadBytes = Base64.getUrlDecoder().decode(payload);

            Map<String, Object> claims = objectMapper.readValue(payloadBytes, new TypeReference<Map<String, Object>>() {
            });
            if (!(claims.get("resource_access") instanceof Map<?, ?> resourceAccess)) {
                return List.of();
            }
            if (!(resourceAccess.get(keycloakProperties.getClientId()) instanceof Map<?, ?> clientAccess)) {
                return List.of();
            }
            if (!(clientAccess.get("roles") instanceof List<?> roles)) {
                return List.of();
            }
            return (List<String>) (List<?>) roles;
        } catch (Exception ex) {
            log.warn("Failed to extract roles from access token", ex);
            return List.of();
        }
    }

    private String createKeycloakUser(String realm, RegisterRequest request) {
        try (Response response = keycloak.realm(realm).users().create(buildUserRepresentation(request))) {
            if (response.getStatus() == 409) {
                throw new UserAlreadyExistsException("Username or email already exists: " + request.getUsername());
            }
            if (response.getStatus() != 201) {
                log.error("Keycloak user creation failed. username={}, status={}", request.getUsername(), response.getStatus());
                throw new RegistrationFailedException("Keycloak returned unexpected status while creating user");
            }
            return CreatedResponseUtil.getCreatedId(response);
        }
    }

    private UserRepresentation buildUserRepresentation(RegisterRequest request) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.getPassword());
        credential.setTemporary(false);

        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setEnabled(true);
        user.setEmailVerified(false);
        user.setCredentials(List.of(credential));
        return user;
    }

    private void assignRole(String realm, String keycloakUserId, UserRole role) {
        String clientId = keycloakProperties.getClientId();
        List<ClientRepresentation> clients = keycloak.realm(realm).clients().findByClientId(clientId);
        if (clients.isEmpty()) {
            throw new RegistrationFailedException("Keycloak client not found: " + clientId);
        }
        String clientUuid = clients.get(0).getId();
        RoleRepresentation roleRepresentation = keycloak.realm(realm)
                .clients().get(clientUuid)
                .roles().get(role.name())
                .toRepresentation();
        keycloak.realm(realm).users().get(keycloakUserId)
                .roles().clientLevel(clientUuid)
                .add(List.of(roleRepresentation));
    }

    private void rollbackKeycloakUser(String realm, String keycloakUserId) {
        try {
            keycloak.realm(realm).users().delete(keycloakUserId);
        } catch (Exception rollbackEx) {
            log.error("Keycloak rollback also failed. userId={}", keycloakUserId, rollbackEx);
        }
    }
}
