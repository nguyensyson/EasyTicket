package com.easytickets.business.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TicketTypeDto {
    private String id;
    private String eventId;
    private String name;
    private BigDecimal price;
    private Integer totalQuantity;
}
