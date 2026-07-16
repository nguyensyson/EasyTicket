package com.easytickets.infratructures.producer;

import com.easytickets.business.dto.event.PaymentFailedEvent;
import com.easytickets.business.dto.event.PaymentSuccessEvent;
import com.easytickets.business.producer.PaymentEventPublisher;
import com.easytickets.common.constant.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Publishes to Kafka topics {@code payment-success} / {@code payment-failed} (message
 * key = orderId). The send is awaited synchronously with a short timeout so callers
 * (PaymentServiceImpl) can log/react if Kafka delivery fails.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer implements PaymentEventPublisher {

    private static final long SEND_TIMEOUT_SECONDS = 3;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishSuccess(PaymentSuccessEvent event) {
        send(AppConstants.TOPIC_PAYMENT_SUCCESS, event.getOrderId(), event);
    }

    @Override
    public void publishFailed(PaymentFailedEvent event) {
        send(AppConstants.TOPIC_PAYMENT_FAILED, event.getOrderId(), event);
    }

    private void send(String topic, String key, Object event) {
        try {
            kafkaTemplate.send(topic, key, event).get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to publish to topic " + topic + ", key=" + key, ex);
        }
    }
}
