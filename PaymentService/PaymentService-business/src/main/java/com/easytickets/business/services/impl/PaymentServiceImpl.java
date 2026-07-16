package com.easytickets.business.services.impl;

import com.easytickets.business.client.OrderServiceClient;
import com.easytickets.business.dto.CallbackResult;
import com.easytickets.business.dto.CreatePaymentRequest;
import com.easytickets.business.dto.FailReason;
import com.easytickets.business.dto.OrderStatus;
import com.easytickets.business.dto.OrderSummaryDto;
import com.easytickets.business.dto.PaymentDto;
import com.easytickets.business.dto.PaymentStatus;
import com.easytickets.business.dto.event.PaymentFailedEvent;
import com.easytickets.business.dto.event.PaymentSuccessEvent;
import com.easytickets.business.exception.OrderNotPayableException;
import com.easytickets.business.exception.OrderServiceUnavailableException;
import com.easytickets.business.exception.PaymentAccessDeniedException;
import com.easytickets.business.exception.PaymentAlreadyExistsException;
import com.easytickets.business.exception.PaymentAlreadyProcessedException;
import com.easytickets.business.exception.PaymentNotFoundException;
import com.easytickets.business.gateway.PaymentGatewaySimulator;
import com.easytickets.business.producer.PaymentEventPublisher;
import com.easytickets.business.repo.PaymentRepo;
import com.easytickets.business.services.PaymentService;
import com.easytickets.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepo paymentRepo;
    private final PaymentEventPublisher paymentEventPublisher;
    private final PaymentGatewaySimulator paymentGatewaySimulator;
    private final OrderServiceClient orderServiceClient;

    @Value("${payment.timeout-minutes:2}")
    private long timeoutMinutes;

    @Override
    public PaymentDto createPayment(CreatePaymentRequest request, String userId) {
        OrderSummaryDto order = fetchOrder(request.getOrderId());

        if (!order.getUserId().equals(userId)) {
            throw new PaymentAccessDeniedException("You do not own order: " + order.getId());
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new OrderNotPayableException("Order is not payable in status " + order.getStatus() + ": " + order.getId());
        }

        PaymentDto payment = paymentRepo.findByOrderId(order.getId())
                .map(existing -> reuseOrRetry(existing, request))
                .orElseGet(() -> createNew(order, request, userId));

        if (payment.getStatus() == PaymentStatus.PENDING) {
            triggerSimulation(payment.getId());
        }
        return payment;
    }

    private PaymentDto reuseOrRetry(PaymentDto existing, CreatePaymentRequest request) {
        if (existing.getStatus() == PaymentStatus.SUCCESS) {
            throw new PaymentAlreadyExistsException("Order already has a successful payment: " + existing.getOrderId());
        }
        if (existing.getStatus() == PaymentStatus.PENDING) {
            log.info("Payment already pending for order, returning existing. orderId={}, paymentId={}",
                    existing.getOrderId(), existing.getId());
            return existing;
        }
        // Previously FAILED – payments.order_id is unique, so retry reuses the same row.
        PaymentDto retried = existing.toBuilder()
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .externalTransactionId(null)
                .failedReason(null)
                .expiresAt(LocalDateTime.now().plusMinutes(timeoutMinutes))
                .build();
        retried = paymentRepo.save(retried);
        log.info("Retrying payment for previously failed order. orderId={}, paymentId={}",
                retried.getOrderId(), retried.getId());
        return retried;
    }

    private PaymentDto createNew(OrderSummaryDto order, CreatePaymentRequest request, String userId) {
        PaymentDto payment = PaymentDto.builder()
                .orderId(order.getId())
                .reservationId(order.getReservationId())
                .userId(userId)
                .amount(order.getTotalAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusMinutes(timeoutMinutes))
                .build();
        payment = paymentRepo.save(payment);
        log.info("Payment created. paymentId={}, orderId={}, userId={}, amount={}",
                payment.getId(), payment.getOrderId(), userId, payment.getAmount());
        return payment;
    }

    private void triggerSimulation(String paymentId) {
        paymentGatewaySimulator.simulate(paymentId, success -> {
            try {
                processCallback(paymentId, success ? CallbackResult.SUCCESS : CallbackResult.FAILED,
                        success ? "MOCK-" + UUID.randomUUID() : null);
            } catch (Exception ex) {
                log.error("Mock gateway callback failed to apply. paymentId={}", paymentId, ex);
            }
        });
    }

    private OrderSummaryDto fetchOrder(String orderId) {
        try {
            ApiResponse<OrderSummaryDto> response = orderServiceClient.getOrder(orderId);
            if (response == null || response.getData() == null) {
                throw new OrderServiceUnavailableException("Order not found or empty response: " + orderId);
            }
            return response.getData();
        } catch (OrderServiceUnavailableException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to fetch order from Order Service. orderId={}", orderId, ex);
            throw new OrderServiceUnavailableException("Order Service unavailable while creating payment");
        }
    }

    @Override
    public PaymentDto getPayment(String paymentId, String callerUserId, boolean isAdmin) {
        PaymentDto payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));

        if (!isAdmin && !payment.getUserId().equals(callerUserId)) {
            throw new PaymentAccessDeniedException("You do not own this payment: " + paymentId);
        }
        return payment;
    }

    @Override
    public PaymentDto processCallback(String paymentId, CallbackResult result, String externalTransactionId) {
        PaymentDto payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));

        PaymentStatus newStatus = result == CallbackResult.SUCCESS ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
        if (payment.getStatus() != PaymentStatus.PENDING) {
            if (payment.getStatus() == newStatus) {
                log.info("Callback already applied, ignoring duplicate delivery. paymentId={}, status={}",
                        paymentId, payment.getStatus());
                return payment;
            }
            throw new PaymentAlreadyProcessedException(
                    "Payment " + paymentId + " already finalized as " + payment.getStatus());
        }

        return finalize(payment, newStatus, newStatus == PaymentStatus.FAILED ? FailReason.DECLINED : null, externalTransactionId);
    }

    @Override
    public void expireTimedOutPayments() {
        List<PaymentDto> expired = paymentRepo.findPendingExpiredBefore(LocalDateTime.now());
        for (PaymentDto payment : expired) {
            try {
                finalize(payment, PaymentStatus.FAILED, FailReason.TIMEOUT, null);
            } catch (Exception ex) {
                log.error("Failed to expire timed-out payment. paymentId={}", payment.getId(), ex);
            }
        }
    }

    private PaymentDto finalize(PaymentDto payment, PaymentStatus newStatus, FailReason failReason, String externalTransactionId) {
        PaymentDto saved = paymentRepo.save(payment.toBuilder()
                .status(newStatus)
                .externalTransactionId(externalTransactionId)
                .failedReason(failReason)
                .build());

        if (newStatus == PaymentStatus.SUCCESS) {
            paymentEventPublisher.publishSuccess(PaymentSuccessEvent.builder()
                    .orderId(saved.getOrderId())
                    .paymentId(saved.getId())
                    .paidAt(LocalDateTime.now())
                    .build());
            log.info("Payment succeeded. paymentId={}, orderId={}", saved.getId(), saved.getOrderId());
        } else {
            paymentEventPublisher.publishFailed(PaymentFailedEvent.builder()
                    .orderId(saved.getOrderId())
                    .reservationId(saved.getReservationId())
                    .reason(failReason.name())
                    .build());
            log.info("Payment failed. paymentId={}, orderId={}, reason={}", saved.getId(), saved.getOrderId(), failReason);
        }
        return saved;
    }
}
