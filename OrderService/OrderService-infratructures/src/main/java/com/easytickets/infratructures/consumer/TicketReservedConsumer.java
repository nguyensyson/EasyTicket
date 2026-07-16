package com.easytickets.infratructures.consumer;

import com.easytickets.business.dto.event.TicketReservedEvent;
import com.easytickets.business.services.OrderService;
import com.easytickets.common.constant.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes {@code ticket-reserved} to create the PENDING_PAYMENT order. Idempotent by
 * {@code reservationId} (checked in {@code OrderServiceImpl.createFromReservation})
 * so at-least-once redelivery never creates a duplicate order.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TicketReservedConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = AppConstants.TOPIC_TICKET_RESERVED, groupId = "order-service")
    public void onMessage(TicketReservedEvent event) {
        log.info("Received ticket-reserved event. reservationId={}, eventId={}, ticketTypeId={}, userId={}",
                event.getReservationId(), event.getEventId(), event.getTicketTypeId(), event.getUserId());
        orderService.createFromReservation(event);
    }
}
