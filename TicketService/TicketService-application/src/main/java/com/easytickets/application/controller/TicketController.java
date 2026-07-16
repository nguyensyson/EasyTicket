package com.easytickets.application.controller;

import com.easytickets.business.dto.PurchaseTicketRequest;
import com.easytickets.business.dto.PurchaseTicketResponse;
import com.easytickets.business.dto.TicketAvailabilityDto;
import com.easytickets.business.services.TicketService;
import com.easytickets.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/{eventId}/purchase")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<PurchaseTicketResponse>> purchaseTicket(@PathVariable String eventId,
                                                                               @Valid @RequestBody PurchaseTicketRequest request,
                                                                               @AuthenticationPrincipal Jwt jwt) {
        PurchaseTicketResponse response = ticketService.purchaseTicket(eventId, request, jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{eventId}/availability")
    public ResponseEntity<ApiResponse<List<TicketAvailabilityDto>>> getAvailability(@PathVariable String eventId) {
        return ResponseEntity.ok(ApiResponse.ok(ticketService.getAvailability(eventId)));
    }

    @PostMapping("/{eventId}/load-inventory")
    @PreAuthorize("hasRole('SYSTEM')")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> loadInventory(@PathVariable String eventId) {
        int loaded = ticketService.loadInventory(eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(Map.of("ticketTypesLoaded", loaded)));
    }
}
