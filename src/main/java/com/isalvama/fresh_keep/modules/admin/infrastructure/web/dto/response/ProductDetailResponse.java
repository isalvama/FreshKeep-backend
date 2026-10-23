package com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProductDetailResponse(
        @Schema(description = "Product id") UUID id,
        @Schema(description = "Product name") String name,
        @Schema(description = "Product type constant name") String productType,
        @Schema(description = "Product expiration date") LocalDate expirationDate,
        @Schema(description = "Id of the storage spot the product is currently stored in") UUID actualStorageSpotId,
        @Schema(description = "Type constant name of the current storage spot") String storageSpotIdType,
        @Schema(description = "userId of the product's creator") UUID creatorId,
        @Schema(description = "Creator's username") String creatorUsername,
        @Schema(description = "Creator's email") String creatorEmail,
        @Schema(description = "Id of the space this product belongs to") UUID spaceId,
        @Schema(description = "Space name") String spaceName,
        @Schema(description = "Store name from the originating shopping receipt") String storeName,
        @Schema(description = "Purchase date from the originating shopping receipt") LocalDate purchaseDate,
        @Schema(description = "Product creation timestamp") Instant createdAt,
        @Schema(description = "Id of the shopping receipt this product was registered from") UUID shoppingReceiptId,
        @Schema(description = "Price, if known") BigDecimal price,
        @Schema(description = "Currency constant name, if known") String currency
) {
}
