package com.easytickets.business.repo;

import com.easytickets.business.dto.PaymentDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepo {

    PaymentDto save(PaymentDto payment);

    Optional<PaymentDto> findById(String id);

    Optional<PaymentDto> findByOrderId(String orderId);

    /**
     * PENDING payments whose {@code expiresAt} is before {@code cutoff} – used by
     * PaymentService-worker's timeout scheduler.
     */
    List<PaymentDto> findPendingExpiredBefore(LocalDateTime cutoff);
}
