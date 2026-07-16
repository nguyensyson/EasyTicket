package com.easytickets.business.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Mirrors Payment Service's payload for Kafka topic {@code payment-success}.
 * Consumed here (idempotent by {@code orderId}) to queue the ticket email message.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessEvent {
    private String orderId;
    private String paymentId;
    private LocalDateTime paidAt;
}
