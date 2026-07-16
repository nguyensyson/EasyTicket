package com.easytickets.infratructures.repo;

import com.easytickets.business.dto.PaymentStatus;
import com.easytickets.infratructures.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByOrderId(String orderId);

    List<Payment> findByStatusAndExpiresAtBefore(PaymentStatus status, LocalDateTime cutoff);
}
