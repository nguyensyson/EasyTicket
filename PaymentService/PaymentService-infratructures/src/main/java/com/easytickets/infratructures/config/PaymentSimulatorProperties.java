package com.easytickets.infratructures.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Tuning knobs for {@code PaymentGatewaySimulatorAdapter}, the stand-in for a real
 * payment gateway (none is integrated yet).
 */
@Configuration
@ConfigurationProperties(prefix = "payment.gateway.simulator")
@Getter
@Setter
public class PaymentSimulatorProperties {

    /** Fraction of simulated payments that resolve as SUCCESS (0.0–1.0). */
    private double successRate = 0.7;

    private int minDelaySeconds = 2;

    private int maxDelaySeconds = 5;
}
