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
    public static final String PAYMENT_NOT_FOUND = "PAYMENT_NOT_FOUND";
    public static final String PAYMENT_ALREADY_PROCESSED = "PAYMENT_ALREADY_PROCESSED";
    public static final String PAYMENT_ALREADY_EXISTS = "PAYMENT_ALREADY_EXISTS";
    public static final String ORDER_NOT_PAYABLE = "ORDER_NOT_PAYABLE";
    public static final String PAYMENT_TIMEOUT = "PAYMENT_TIMEOUT";
    public static final String PAYMENT_FAILED = "PAYMENT_FAILED";
    public static final String ORDER_SERVICE_UNAVAILABLE = "ORDER_SERVICE_UNAVAILABLE";
    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";

    public static final String TOPIC_PAYMENT_SUCCESS = "payment-success";
    public static final String TOPIC_PAYMENT_FAILED = "payment-failed";
}
