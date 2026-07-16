package com.easytickets.business.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizerHistoryDto {
    private long totalEvents;
    private long totalTicketsSold;
    private BigDecimal totalRevenue;
    private List<OrganizerEventStatsDto> events;
}
