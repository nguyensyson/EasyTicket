package com.easytickets.worker.scheduler;

import com.easytickets.business.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Sweeps PENDING payments past their 2-minute {@code expiresAt} and fails them with
 * reason TIMEOUT (publishing {@code payment-failed} via PaymentServiceImpl).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentTimeoutScheduler {

    private final PaymentService paymentService;

    @Scheduled(fixedRateString = "${payment.timeout-check-interval-ms:15000}")
    public void checkTimeouts() {
        try {
            paymentService.expireTimedOutPayments();
        } catch (Exception ex) {
            log.error("Payment timeout sweep failed", ex);
        }
    }
}
