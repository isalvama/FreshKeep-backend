package com.isalvama.fresh_keep.modules.product.infrastructure.web.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateProductRequest (

        @Size(max= 30)
        @Pattern(regexp = ".*\\S.*")
        String name,

        LocalDate expirationDate,

        @Size(max= 30)
        @Pattern(regexp = ".*\\S.*")
        String productType,

        @Positive
        BigDecimal amount,

        @Size(max= 20)
        @Pattern(regexp = ".*\\S.*")
        String currency
) {
}
