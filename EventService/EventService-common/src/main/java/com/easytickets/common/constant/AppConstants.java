package com.easytickets.common.constant;

/**
 * Application-wide error code constants used in ApiResponse and BusinessException.
 */
public final class AppConstants {

    private AppConstants() {
        // Utility class – prevent instantiation
    }

    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String INVALID_TOKEN = "INVALID_TOKEN";
    public static final String FORBIDDEN = "FORBIDDEN";
    public static final String EVENT_NOT_FOUND = "EVENT_NOT_FOUND";
    public static final String EVENT_ALREADY_PUBLISHED = "EVENT_ALREADY_PUBLISHED";
    public static final String FLASH_SALE_SCHEDULE_CONFLICT = "FLASH_SALE_SCHEDULE_CONFLICT";
    public static final String TICKET_SERVICE_UNAVAILABLE = "TICKET_SERVICE_UNAVAILABLE";
    public static final String ORDER_SERVICE_UNAVAILABLE = "ORDER_SERVICE_UNAVAILABLE";
    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
}
