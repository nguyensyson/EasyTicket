package com.easytickets.business.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Mirrors the per-event aggregate returned by Order Service's
 * {@code GET /api/v1/orders/stats/by-events} (own copy – no shared JAR between
 * services).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventOrderStatsDto {
    private String eventId;
    private long ticketsSold;
    private BigDecimal revenue;
}
