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
public class EventSearchCriteria {
    private String categoryId;
    private String locationId;
    private LocalDateTime from;
    private LocalDateTime to;
    private int page;
    private int size;
}
