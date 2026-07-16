package com.easytickets.infratructures.consumer;

import com.easytickets.business.dto.event.PaymentFailedEvent;
import com.easytickets.business.services.OrderService;
import com.easytickets.common.constant.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes {@code payment-failed} to mark the order CANCELLED. Idempotent by
 * {@code orderId} (checked in {@code OrderServiceImpl.markCancelled}). Ticket
 * Service's own consumer of this topic (releasing stock back to Redis) is a
 * separate concern, not implemented here.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentFailedConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = AppConstants.TOPIC_PAYMENT_FAILED, groupId = "order-service")
    public void onMessage(PaymentFailedEvent event) {
        log.info("Received payment-failed event. orderId={}, reservationId={}, reason={}",
                event.getOrderId(), event.getReservationId(), event.getReason());
        orderService.markCancelled(event);
    }
}
