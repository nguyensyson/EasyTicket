package com.easytickets.infratructures.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;

@Entity
@Table(name = "ticket_types")
@SQLRestriction("delete_flag = 'ACTIVE'")
@Data
@EqualsAndHashCode(callSuper = true)
@EntityListeners(AuditingEntityListener.class)
public class TicketType extends BaseEntity {

    @Column(name = "event_id", length = 36, nullable = false)
    private String eventId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "price", precision = 12, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;
}
