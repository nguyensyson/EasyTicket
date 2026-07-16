package com.easytickets.business.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Kafka message payload published to topic {@code payment-success}. Consumed by
 * Order Service (marks the order PAID) and Notification Service (sends the e-ticket).
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
