package com.easytickets.application.controller;

import com.easytickets.business.dto.LoginRequest;
import com.easytickets.business.dto.LoginResponse;
import com.easytickets.business.dto.OrganizerHistoryDto;
import com.easytickets.business.dto.RegisterRequest;
import com.easytickets.business.dto.RegisterResponse;
import com.easytickets.business.dto.TicketHistoryDto;
import com.easytickets.business.dto.UserRole;
import com.easytickets.business.services.UserService;
import com.easytickets.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/register/buyer")
    public ResponseEntity<ApiResponse<RegisterResponse>> registerBuyer(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = userService.register(request, UserRole.BUYER);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PostMapping("/register/organizer")
    public ResponseEntity<ApiResponse<RegisterResponse>> registerOrganizer(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = userService.register(request, UserRole.ORGANIZER);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PostMapping("/register/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RegisterResponse>> registerAdmin(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = userService.register(request, UserRole.ADMIN);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/me/ticket-history")
    public ResponseEntity<ApiResponse<List<TicketHistoryDto>>> getTicketHistory() {
        return ResponseEntity.ok(ApiResponse.ok(userService.getTicketHistory()));
    }

    @GetMapping("/me/organizer-history")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ApiResponse<OrganizerHistoryDto>> getOrganizerHistory() {
        return ResponseEntity.ok(ApiResponse.ok(userService.getOrganizerHistory()));
    }
}
