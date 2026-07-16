package com.easytickets.business.services.impl;

import com.easytickets.business.config.KeycloakConfigProperties;
import com.easytickets.business.dto.RegisterRequest;
import com.easytickets.business.dto.RegisterResponse;
import com.easytickets.business.dto.UserProfileDto;
import com.easytickets.business.dto.UserRole;
import com.easytickets.business.exception.RegistrationFailedException;
import com.easytickets.business.exception.UserAlreadyExistsException;
import com.easytickets.business.repo.UserProfileRepo;
import com.easytickets.business.services.UserService;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final Keycloak keycloak;
    private final KeycloakConfigProperties keycloakProperties;
    private final UserProfileRepo userProfileRepo;

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
