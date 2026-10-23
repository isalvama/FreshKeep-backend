package com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ProductInfoResponse(
        @Schema(description = "Product id") UUID id,
        @Schema(description = "Product name") String name,
        @Schema(description = "Product expiration date") LocalDate expirationDate,
        @Schema(description = "Id of the storage spot the product is currently stored in") UUID actualStorageSpotId,
        @Schema(description = "Product type constant name") String productType,
        @Schema(description = "Id of the shopping receipt this product was registered from") UUID shoppingReceiptId,
        @Schema(description = "Price, if known") BigDecimal price,
        @Schema(description = "Currency constant name, if known") String currency
) {
}
