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
public class NotificationDto {
    private String id;
    private String orderId;
    private NotificationType type;
    private NotificationChannel channel;
    private NotificationStatus status;
    private String errorMessage;
    private LocalDateTime queuedAt;
    private String createdBy;
}
