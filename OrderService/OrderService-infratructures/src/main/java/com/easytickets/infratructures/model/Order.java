package com.easytickets.infratructures.model;

import com.easytickets.business.dto.OrderStatus;
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

@Entity
@Table(name = "orders")
@SQLRestriction("delete_flag = 'ACTIVE'")
@Data
@EqualsAndHashCode(callSuper = true)
@EntityListeners(AuditingEntityListener.class)
public class Order extends BaseEntity {

    @Column(name = "reservation_id", length = 255, nullable = false, unique = true)
    private String reservationId;

    @Column(name = "user_id", length = 255, nullable = false)
    private String userId;

    @Column(name = "event_id", length = 36, nullable = false)
    private String eventId;

    @Column(name = "ticket_type_id", length = 36, nullable = false)
    private String ticketTypeId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "total_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "payment_id", length = 36)
    private String paymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;
}
