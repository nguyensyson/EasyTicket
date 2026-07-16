package com.easytickets.business.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

    @NotBlank(message = "orderId is required")
    private String orderId;

    @NotNull(message = "paymentMethod is required")
    private PaymentMethod paymentMethod;
}
