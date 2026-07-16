package com.easytickets.application.controller;

import com.easytickets.business.dto.RegisterRequest;
import com.easytickets.business.dto.RegisterResponse;
import com.easytickets.business.dto.UserRole;
import com.easytickets.business.services.UserService;
import com.easytickets.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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
}
