package com.easytickets.application.controller;

import com.easytickets.business.dto.CreatePaymentRequest;
import com.easytickets.business.dto.PaymentCallbackRequest;
import com.easytickets.business.dto.PaymentDto;
import com.easytickets.business.services.PaymentService;
import com.easytickets.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<PaymentDto>> createPayment(@Valid @RequestBody CreatePaymentRequest request,
                                                                  @AuthenticationPrincipal Jwt jwt) {
        PaymentDto payment = paymentService.createPayment(request, jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(payment));
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('BUYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<PaymentDto>> getPayment(@PathVariable String paymentId,
                                                               @AuthenticationPrincipal Jwt jwt,
                                                               Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(ROLE_ADMIN));
        PaymentDto payment = paymentService.getPayment(paymentId, jwt.getSubject(), isAdmin);
        return ResponseEntity.ok(ApiResponse.ok(payment));
    }

    /**
     * Webhook the gateway calls with the final result. No real gateway is integrated
     * yet (signature verification is therefore skipped) – payments normally resolve on
     * their own via the in-process simulator, but this endpoint remains available for
     * manually driving/testing a specific outcome.
     */
    @PostMapping("/{paymentId}/callback")
    public ResponseEntity<ApiResponse<PaymentDto>> callback(@PathVariable String paymentId,
                                                             @Valid @RequestBody PaymentCallbackRequest request) {
        PaymentDto payment = paymentService.processCallback(paymentId, request.getResult(), request.getExternalTransactionId());
        return ResponseEntity.ok(ApiResponse.ok(payment));
    }
}
