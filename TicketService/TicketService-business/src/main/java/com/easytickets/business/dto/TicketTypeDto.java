package com.easytickets.business.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Mirrors EventService's TicketTypeDto contract – deserialized from the
 * GET /api/v1/events/{eventId}/ticket-types response via {@link com.easytickets.business.client.EventServiceClient}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketTypeDto {
    private String id;
    private String eventId;
    private String name;
    private BigDecimal price;
    private Integer totalQuantity;
}
