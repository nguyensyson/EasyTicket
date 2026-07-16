package com.easytickets.business.producer;

import com.easytickets.business.dto.event.PaymentFailedEvent;
import com.easytickets.business.dto.event.PaymentSuccessEvent;

/**
 * Port for publishing to Kafka topics {@code payment-success} / {@code payment-failed}.
 * Implemented in {@code PaymentService-infratructures} with a KafkaTemplate-backed adapter.
 */
public interface PaymentEventPublisher {

    void publishSuccess(PaymentSuccessEvent event);

    void publishFailed(PaymentFailedEvent event);
}
