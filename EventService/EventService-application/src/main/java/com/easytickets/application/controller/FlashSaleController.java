package com.easytickets.application.controller;

import com.easytickets.business.dto.CreateFlashSaleRequest;
import com.easytickets.business.dto.FlashSaleDto;
import com.easytickets.business.services.FlashSaleService;
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

@RestController
@RequestMapping("api/v1/events/{eventId}/flash-sale")
@RequiredArgsConstructor
public class FlashSaleController {

    private final FlashSaleService flashSaleService;

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ApiResponse<FlashSaleDto>> createFlashSale(@PathVariable String eventId,
                                                                      @Valid @RequestBody CreateFlashSaleRequest request,
                                                                      @AuthenticationPrincipal Jwt jwt) {
        FlashSaleDto created = flashSaleService.createFlashSale(eventId, request, jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<FlashSaleDto>> getFlashSale(@PathVariable String eventId) {
        return ResponseEntity.ok(ApiResponse.ok(flashSaleService.getFlashSale(eventId)));
    }
}
