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
    public static final String ORDER_NOT_FOUND = "ORDER_NOT_FOUND";
    public static final String ORDER_ALREADY_PAID = "ORDER_ALREADY_PAID";
    public static final String ORDER_ALREADY_CANCELLED = "ORDER_ALREADY_CANCELLED";
    public static final String ORDER_STATE_CONFLICT = "ORDER_STATE_CONFLICT";
    public static final String PAYMENT_SERVICE_UNAVAILABLE = "PAYMENT_SERVICE_UNAVAILABLE";
    public static final String TICKET_SERVICE_UNAVAILABLE = "TICKET_SERVICE_UNAVAILABLE";
    public static final String EVENT_SERVICE_UNAVAILABLE = "EVENT_SERVICE_UNAVAILABLE";
    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
}
