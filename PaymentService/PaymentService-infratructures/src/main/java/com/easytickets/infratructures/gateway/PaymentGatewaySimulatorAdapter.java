package com.easytickets.infratructures.gateway;

import com.easytickets.business.gateway.PaymentGatewaySimulator;
import com.easytickets.infratructures.config.PaymentSimulatorProperties;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * No real payment gateway is integrated yet – this simulates one by resolving each
 * payment to a random SUCCESS/FAILED outcome after a short delay, off the calling
 * (HTTP request) thread.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentGatewaySimulatorAdapter implements PaymentGatewaySimulator {

    private final PaymentSimulatorProperties properties;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "payment-gateway-simulator");
        thread.setDaemon(true);
        return thread;
    });

    @Override
    public void simulate(String paymentId, Consumer<Boolean> onResult) {
        long delaySeconds = ThreadLocalRandom.current().nextLong(properties.getMinDelaySeconds(), properties.getMaxDelaySeconds() + 1);
        boolean success = ThreadLocalRandom.current().nextDouble() < properties.getSuccessRate();

        log.info("Mock gateway scheduled. paymentId={}, delaySeconds={}, willSucceed={}", paymentId, delaySeconds, success);
        executor.schedule(() -> {
            try {
                onResult.accept(success);
            } catch (Exception ex) {
                log.error("Mock gateway result handler threw. paymentId={}", paymentId, ex);
            }
        }, delaySeconds, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }
}
