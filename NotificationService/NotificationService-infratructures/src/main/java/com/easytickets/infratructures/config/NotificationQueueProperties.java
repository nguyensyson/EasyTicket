package com.easytickets.infratructures.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "notification.queue")
@Getter
@Setter
public class NotificationQueueProperties {
    private String ticketEmailUrl;
}
