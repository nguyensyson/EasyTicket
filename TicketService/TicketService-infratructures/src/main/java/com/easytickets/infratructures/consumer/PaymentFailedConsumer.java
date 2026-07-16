package com.easytickets.infratructures.consumer;

import com.easytickets.business.dto.event.PaymentFailedEvent;
import com.easytickets.business.services.TicketService;
import com.easytickets.common.constant.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentFailedConsumer {

    private final TicketService ticketService;

    @KafkaListener(topics = AppConstants.TOPIC_PAYMENT_FAILED, groupId = "${spring.kafka.consumer.group-id}")
    public void onPaymentFailed(PaymentFailedEvent event) {
        log.info("Received payment-failed. orderId={}, reservationId={}, reason={}",
                event.getOrderId(), event.getReservationId(), event.getReason());
        ticketService.releaseReservation(event);
    }
}
