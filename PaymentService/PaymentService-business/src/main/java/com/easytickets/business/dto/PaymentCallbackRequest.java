package com.easytickets.business.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload of the payment gateway webhook: {@code POST /api/v1/payments/{paymentId}/callback}.
 * In production this would arrive signed from the real gateway; since no gateway is
 * integrated yet, the endpoint also doubles as a manual test entrypoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCallbackRequest {

    @NotNull(message = "result is required")
    private CallbackResult result;

    private String externalTransactionId;
}
