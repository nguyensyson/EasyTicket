package com.easytickets.business.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mirrors Payment Service's payload for Kafka topic {@code payment-failed}.
 * Consumed here (idempotent by {@code orderId}) to mark the order CANCELLED.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailedEvent {
    private String orderId;
    private String reservationId;
    private String reason;
}
