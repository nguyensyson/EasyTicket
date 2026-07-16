package com.easytickets.business.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketReservationDto {
    private String userId;
    private String eventId;
    private String ticketTypeId;
    private int quantity;
    private BigDecimal unitPrice;
}
