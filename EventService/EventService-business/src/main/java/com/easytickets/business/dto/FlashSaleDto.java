package com.easytickets.business.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashSaleDto {
    private String id;
    private String eventId;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private FlashSaleStatus status;
}
