package com.isalvama.fresh_keep.modules.product.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateProductRequest (

        @NotBlank
        String name,

        @NotNull
        LocalDate expirationDate,

        @NotBlank
        String productType,

        @NotNull
        BigDecimal amount,

        @NotBlank
        String currency
) {
}
