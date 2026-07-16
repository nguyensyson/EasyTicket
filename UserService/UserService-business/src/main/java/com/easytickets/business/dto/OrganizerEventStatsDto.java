package com.easytickets.business.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Mirrors Event Service's OrganizerEventStatsDto (own copy – no shared JAR
 * between services).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizerEventStatsDto {
    private String eventId;
    private String title;
    private EventStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long ticketsSold;
    private BigDecimal revenue;
}
