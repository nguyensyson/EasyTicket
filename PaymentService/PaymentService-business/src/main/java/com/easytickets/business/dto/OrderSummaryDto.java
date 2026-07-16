package com.easytickets.business.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Subset of Order Service's OrderDto fetched via {@code OrderServiceClient} –
 * only the fields Payment Service needs to validate and price a payment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryDto {
    private String id;
    private String reservationId;
    private String userId;
    private BigDecimal totalAmount;
    private OrderStatus status;
}
