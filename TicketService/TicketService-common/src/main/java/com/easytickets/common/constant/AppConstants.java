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
    public static final String TICKET_NOT_FOUND = "TICKET_NOT_FOUND";
    public static final String TICKET_SOLD_OUT = "TICKET_SOLD_OUT";
    public static final String INVENTORY_UNAVAILABLE = "INVENTORY_UNAVAILABLE";
    public static final String RESERVATION_FAILED = "RESERVATION_FAILED";
    public static final String EVENT_SERVICE_UNAVAILABLE = "EVENT_SERVICE_UNAVAILABLE";
    public static final String ORDER_SERVICE_UNAVAILABLE = "ORDER_SERVICE_UNAVAILABLE";
    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";

    public static final String TOPIC_TICKET_RESERVED = "ticket-reserved";
}
