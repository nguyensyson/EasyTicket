package com.easytickets.common.constant;

/**
 * Application-wide error code constants used in ApiResponse and BusinessException.
 */
public final class AppConstants {

    private AppConstants() {
        // Utility class – prevent instantiation
    }

    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String INVALID_TOKEN = "INVALID_TOKEN";
    public static final String FORBIDDEN = "FORBIDDEN";
    public static final String PROFILE_NOT_FOUND = "PROFILE_NOT_FOUND";
    public static final String USER_ALREADY_EXISTS = "USER_ALREADY_EXISTS";
    public static final String REGISTRATION_FAILED = "REGISTRATION_FAILED";
    public static final String KEYCLOAK_UNAVAILABLE = "KEYCLOAK_UNAVAILABLE";
    public static final String ORDER_SERVICE_UNAVAILABLE = "ORDER_SERVICE_UNAVAILABLE";
    public static final String EVENT_SERVICE_UNAVAILABLE = "EVENT_SERVICE_UNAVAILABLE";
    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
}
