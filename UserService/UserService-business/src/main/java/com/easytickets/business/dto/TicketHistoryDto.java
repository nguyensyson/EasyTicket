package com.easytickets.business.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Mirrors Order Service's OrderDto (own copy – no shared JAR between services).
 * Returned as-is by {@code GET /api/v1/users/me/ticket-history}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketHistoryDto {
    private String id;
    private String reservationId;
    private String eventId;
    private String ticketTypeId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private String paymentId;
    private OrderStatus status;
}
