package com.easytickets.business.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Kafka message payload published to topic {@code ticket-reserved} after the Redis
 * Lua CHECK & DECREMENT script reserves stock. Consumed by Order Service to create
 * the PENDING_PAYMENT order (idempotent by {@code reservationId}).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketReservedEvent {
    private String reservationId;
    private String userId;
    private String eventId;
    private String ticketTypeId;
    private Integer quantity;
    private BigDecimal unitPrice;
}
