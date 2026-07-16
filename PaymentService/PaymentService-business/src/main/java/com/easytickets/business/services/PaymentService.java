package com.easytickets.business.services;

import com.easytickets.business.dto.CallbackResult;
import com.easytickets.business.dto.CreatePaymentRequest;
import com.easytickets.business.dto.PaymentDto;

public interface PaymentService {

    /**
     * Initiates a payment for an order's PENDING_PAYMENT state (fetches and validates
     * the order via Order Service, then creates/reuses a PENDING payment row and kicks
     * off the mock gateway simulation). Not idempotent across different orders, but
     * safe to retry for the same order (returns/reactivates the existing row, since
     * {@code payments.order_id} is unique).
     */
    PaymentDto createPayment(CreatePaymentRequest request, String userId);

    /**
     * Fetches a payment for display. Buyers may only see their own payments; admins
     * may see any payment.
     */
    PaymentDto getPayment(String paymentId, String callerUserId, boolean isAdmin);

    /**
     * Finalizes a PENDING payment with a gateway result – called both by the real
     * webhook controller and by the in-process gateway simulator. Idempotent: a
     * repeated callback with the same result is a no-op; a conflicting result on an
     * already-finalized payment is rejected.
     */
    PaymentDto processCallback(String paymentId, CallbackResult result, String externalTransactionId);

    /**
     * Finds PENDING payments past their 2-minute {@code expiresAt} and fails them with
     * reason TIMEOUT. Called by PaymentService-worker's scheduler.
     */
    void expireTimedOutPayments();
}
