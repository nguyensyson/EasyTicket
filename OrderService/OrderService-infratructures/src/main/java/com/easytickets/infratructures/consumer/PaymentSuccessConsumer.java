package com.easytickets.infratructures.consumer;

import com.easytickets.business.dto.event.PaymentSuccessEvent;
import com.easytickets.business.services.OrderService;
import com.easytickets.common.constant.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes {@code payment-success} to mark the order PAID. Idempotent by
 * {@code orderId} (checked in {@code OrderServiceImpl.markPaid}).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentSuccessConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = AppConstants.TOPIC_PAYMENT_SUCCESS, groupId = "order-service")
    public void onMessage(PaymentSuccessEvent event) {
        log.info("Received payment-success event. orderId={}, paymentId={}", event.getOrderId(), event.getPaymentId());
        orderService.markPaid(event);
    }
}
