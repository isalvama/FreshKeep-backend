package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record ShoppingReceiptResponse(
        @Schema(description = "Shopping receipt id") String id,
        @Schema(description = "Final purchase date persisted for this receipt") LocalDate shoppingDate,
        @Schema(description = "Store name") String storeName,
        @Schema(description = "Persisted products, sorted by expirationDate ascending (soonest-to-expire first), nulls last") List<ShoppingReceiptProductResponse> products,
        @Schema(description = "This space's storage spots") List<SuggestedStorageSpotResponse> storageSpots
) {
}
