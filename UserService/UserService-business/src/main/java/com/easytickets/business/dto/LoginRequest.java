package com.easytickets.business.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank
    @Size(min = 1, max = 255)
    private String username;

    @NotBlank
    @Size(min = 1, max = 255)
    private String password;
}
