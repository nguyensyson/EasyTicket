package com.easytickets.business.dto;

/**
 * Mirrors Order Service's order status values (own copy – no shared JAR
 * between services). Used to interpret the {@code status} field returned by
 * {@code OrderServiceClient}.
 */
public enum OrderStatus {
    PENDING_PAYMENT,
    PAID,
    CANCELLED
}
