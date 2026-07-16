package com.easytickets.business.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Mirrors Ticket Service's payload for Kafka topic {@code ticket-reserved}.
 * Consumed here (idempotent by {@code reservationId}) to create the PENDING_PAYMENT order.
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
