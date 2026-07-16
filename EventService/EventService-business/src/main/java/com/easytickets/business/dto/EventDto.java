package com.easytickets.business.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {
    private String id;
    private String organizerId;
    private String title;
    private String description;
    private EventCategory category;
    private String locationId;
    private String location;
    private String bannerUrl;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private EventStatus status;
}
