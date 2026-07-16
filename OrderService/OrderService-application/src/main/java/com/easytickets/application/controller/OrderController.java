package com.easytickets.application.controller;

import com.easytickets.business.dto.EventOrderStatsDto;
import com.easytickets.business.dto.OrderDto;
import com.easytickets.business.services.OrderService;
import com.easytickets.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final OrderService orderService;

    @GetMapping("/my-tickets")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getMyTickets(@AuthenticationPrincipal Jwt jwt) {
        List<OrderDto> tickets = orderService.getMyTickets(jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.ok(tickets));
    }

    /**
     * Internal aggregation endpoint consumed by Event Service (via Feign, forwarding
     * the calling Organizer's JWT) to build the {@code organizer-history} view.
     * Event ownership is validated on the Event Service side before this is called.
     */
    @GetMapping("/stats/by-events")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ApiResponse<List<EventOrderStatsDto>>> getStatsByEvents(
            @RequestParam List<String> eventIds) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getStatsByEventIds(eventIds)));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('BUYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderDto>> getOrder(@PathVariable String orderId,
                                                           @AuthenticationPrincipal Jwt jwt,
                                                           Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(ROLE_ADMIN));
        OrderDto order = orderService.getOrder(orderId, jwt.getSubject(), isAdmin);
        return ResponseEntity.ok(ApiResponse.ok(order));
    }
}
