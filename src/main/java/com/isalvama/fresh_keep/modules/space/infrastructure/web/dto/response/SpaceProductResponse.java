package com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SpaceProductResponse(
        @Schema(description = "Product id") String id,
        @Schema(description = "Product name") String productName,
        @Schema(description = "Product expiration date") LocalDate expirationDate,
        @Schema(description = "Id of the storage spot this product is currently stored in") String storageSpotId,
        @Schema(description = "Product type constant name") String productType,
        @Schema(description = "Price amount, if known") BigDecimal priceAmount,
        @Schema(description = "Currency constant name, if known") String currency
) {
}
