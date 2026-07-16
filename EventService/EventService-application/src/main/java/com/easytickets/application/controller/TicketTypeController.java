package com.easytickets.application.controller;

import com.easytickets.business.dto.CreateTicketTypeRequest;
import com.easytickets.business.dto.TicketTypeDto;
import com.easytickets.business.dto.UpdateTicketTypeRequest;
import com.easytickets.business.services.TicketTypeService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/events/{eventId}/ticket-types")
@RequiredArgsConstructor
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ApiResponse<TicketTypeDto>> createTicketType(@PathVariable String eventId,
                                                                        @Valid @RequestBody CreateTicketTypeRequest request,
                                                                        @AuthenticationPrincipal Jwt jwt) {
        TicketTypeDto created = ticketTypeService.createTicketType(eventId, request, jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @PutMapping("/{ticketTypeId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ApiResponse<TicketTypeDto>> updateTicketType(@PathVariable String eventId,
                                                                        @PathVariable String ticketTypeId,
                                                                        @Valid @RequestBody UpdateTicketTypeRequest request,
                                                                        @AuthenticationPrincipal Jwt jwt) {
        TicketTypeDto updated = ticketTypeService.updateTicketType(eventId, ticketTypeId, request, jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TicketTypeDto>>> listTicketTypes(@PathVariable String eventId) {
        return ResponseEntity.ok(ApiResponse.ok(ticketTypeService.listTicketTypes(eventId)));
    }
}
