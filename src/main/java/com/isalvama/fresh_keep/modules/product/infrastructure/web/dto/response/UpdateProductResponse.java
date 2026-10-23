package com.isalvama.fresh_keep.modules.product.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateProductResponse(
        @Schema(description = "Product id") String productId,
        @Schema(description = "Product name") String name,
        @Schema(description = "Product expiration date") LocalDate expirationDate,
        @Schema(description = "Product type constant name") String productType,
        @Schema(description = "Price amount, if known") BigDecimal amount,
        @Schema(description = "Currency constant name, if known") String currency
) {
}
