package com.easytickets.infratructures.model;

import com.easytickets.business.dto.FailReason;
import com.easytickets.business.dto.PaymentMethod;
import com.easytickets.business.dto.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@SQLRestriction("delete_flag = 'ACTIVE'")
@Data
@EqualsAndHashCode(callSuper = true)
@EntityListeners(AuditingEntityListener.class)
public class Payment extends BaseEntity {

    @Column(name = "order_id", length = 36, nullable = false, unique = true)
    private String orderId;

    @Column(name = "reservation_id", length = 255)
    private String reservationId;

    @Column(name = "user_id", length = 255, nullable = false)
    private String userId;

    @Column(name = "amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "external_transaction_id", length = 255)
    private String externalTransactionId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "failed_reason")
    private FailReason failedReason;
}
