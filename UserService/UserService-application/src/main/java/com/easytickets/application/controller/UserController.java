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
}
