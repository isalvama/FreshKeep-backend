package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record ProcessNewShoppingReceiptResponse(
        @Schema(description = "Id of the draft ShoppingReceipt; send back to reprocess/confirm to finalize it") String shoppingReceiptId,
        @Schema(description = "Id of the persisted ReceiptImage") String receiptImageId,
        @Schema(description = "This space's storage spots") List<SuggestedStorageSpotResponse> suggestedStorageSpots,
        @Schema(description = "Already rectified server-side; trust this over whatever the AI originally read off the receipt") LocalDate purchaseShoppingDate,
        @Schema(description = "Store name extracted by the AI") String storeName,
        @Schema(description = "Full list of products the AI extracted") List<ProductResponse> productExtractions,
        @Schema(description = "Subset of productExtractions the AI itself flagged as low-confidence/worth reviewing") List<ProductResponse> flaggedProducts
)
{
}
