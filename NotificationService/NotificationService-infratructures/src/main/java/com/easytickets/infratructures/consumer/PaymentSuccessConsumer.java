package com.easytickets.infratructures.consumer;

import com.easytickets.business.dto.event.PaymentSuccessEvent;
import com.easytickets.business.services.NotificationService;
import com.easytickets.common.constant.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes {@code payment-success} to queue the ticket email message. Idempotent by
 * {@code orderId} (checked in {@code NotificationServiceImpl.processPaymentSuccess}).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentSuccessConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = AppConstants.TOPIC_PAYMENT_SUCCESS, groupId = "${spring.kafka.consumer.group-id}")
    public void onPaymentSuccess(PaymentSuccessEvent event) {
        log.info("Received payment-success event. orderId={}, paymentId={}", event.getOrderId(), event.getPaymentId());
        notificationService.processPaymentSuccess(event);
    }
}
