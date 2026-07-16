package com.easytickets.business.producer;

import com.easytickets.business.dto.event.TicketReservedEvent;

/**
 * Port for publishing to Kafka topic {@code ticket-reserved}. Implemented in
 * {@code TicketService-infratructures} with a KafkaTemplate-backed adapter.
 */
public interface TicketReservedEventPublisher {
    void publish(TicketReservedEvent event);
}
