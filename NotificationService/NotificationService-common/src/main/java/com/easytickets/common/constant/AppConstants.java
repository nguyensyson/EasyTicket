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
    public static final String NOTIFICATION_NOT_FOUND = "NOTIFICATION_NOT_FOUND";
    public static final String EMAIL_SEND_FAILED = "EMAIL_SEND_FAILED";
    public static final String QR_GENERATION_FAILED = "QR_GENERATION_FAILED";
    public static final String NOTIFICATION_QUEUE_FAILED = "NOTIFICATION_QUEUE_FAILED";
    public static final String USER_SERVICE_UNAVAILABLE = "USER_SERVICE_UNAVAILABLE";
    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";

    public static final String TOPIC_PAYMENT_SUCCESS = "payment-success";
}
