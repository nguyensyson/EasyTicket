package com.easytickets.business.dto;

/**
 * Mirrors Event Service's event status values (own copy – no shared JAR
 * between services). Used to interpret the {@code status} field returned by
 * {@code EventServiceClient}.
 */
public enum EventStatus {
    DRAFT, PUBLISHED, CANCELLED
}
