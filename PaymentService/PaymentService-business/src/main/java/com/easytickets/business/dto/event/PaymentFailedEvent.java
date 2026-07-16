package com.easytickets.business.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kafka message payload published to topic {@code payment-failed}. Consumed by Order
 * Service (marks the order CANCELLED) and Ticket Service (releases the reservation
 * back to Redis inventory). {@code reason} is {@code DECLINED} or {@code TIMEOUT}.
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
