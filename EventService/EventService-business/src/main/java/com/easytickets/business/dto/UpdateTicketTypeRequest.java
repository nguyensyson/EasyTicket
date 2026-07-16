package com.easytickets.business.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateTicketTypeRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal price;

    @NotNull
    @Min(1)
    private Integer totalQuantity;
}
