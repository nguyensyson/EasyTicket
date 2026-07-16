package com.easytickets.business.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Maps the JSON body returned by Keycloak's OpenID Connect token endpoint.
 * Only the fields UserService needs are declared; the rest are ignored.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeycloakTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;
}
