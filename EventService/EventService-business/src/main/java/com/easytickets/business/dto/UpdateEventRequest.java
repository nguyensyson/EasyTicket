package com.easytickets.business.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateEventRequest {

    @NotBlank
    @Size(max = 255)
    private String title;

    private String description;

    @NotNull
    private EventCategory category;

    @NotBlank
    @Size(max = 36)
    private String locationId;

    @NotBlank
    @Size(max = 255)
    private String location;

    @Size(max = 255)
    private String bannerUrl;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;

    @NotNull
    private EventStatus status;
}
