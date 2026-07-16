package com.easytickets.business.exception;

import com.easytickets.common.constant.AppConstants;
import org.springframework.http.HttpStatus;

public class KeycloakUnavailableException extends BusinessException {

    public KeycloakUnavailableException(String message) {
        super(AppConstants.KEYCLOAK_UNAVAILABLE, message, HttpStatus.BAD_GATEWAY);
    }

    public KeycloakUnavailableException(String message, Throwable cause) {
        super(AppConstants.KEYCLOAK_UNAVAILABLE, message, HttpStatus.BAD_GATEWAY);
        initCause(cause);
    }
}
