package com.easytickets.infratructures.producer;

import com.easytickets.business.dto.event.TicketReservedEvent;
import com.easytickets.business.producer.TicketReservedEventPublisher;
import com.easytickets.common.constant.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Publishes to Kafka topic {@code ticket-reserved} (message key = eventId, to preserve
 * per-event ordering). The send is awaited synchronously with a short timeout so the
 * caller (TicketServiceImpl) can release the reserved stock if delivery fails.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TicketReservedProducer implements TicketReservedEventPublisher {

    private static final long SEND_TIMEOUT_SECONDS = 3;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publish(TicketReservedEvent event) {
        try {
            kafkaTemplate.send(AppConstants.TOPIC_TICKET_RESERVED, event.getEventId(), event)
                    .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to publish ticket-reserved event, reservationId=" + event.getReservationId(), ex);
        }
    }
}
