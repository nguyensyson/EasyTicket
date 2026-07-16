package com.easytickets.business.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateFlashSaleRequest {

    @NotNull
    @Future
    private LocalDateTime startAt;

    @NotNull
    @Future
    private LocalDateTime endAt;
}
