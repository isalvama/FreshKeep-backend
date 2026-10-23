package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ShoppingReceiptProductResponse(
        @Schema(description = "Persisted Product id") String id,
        @Schema(description = "Product name") String productName,
        @Schema(description = "Product expiration date") LocalDate expirationDate,
        @Schema(description = "Id of the storage spot the product is stored in") String storageSpotId,
        @Schema(description = "Product type constant name") String productType,
        @Schema(description = "Price, if known") BigDecimal priceAmount,
        @Schema(description = "Currency constant name, if known") String currency
) {
}
