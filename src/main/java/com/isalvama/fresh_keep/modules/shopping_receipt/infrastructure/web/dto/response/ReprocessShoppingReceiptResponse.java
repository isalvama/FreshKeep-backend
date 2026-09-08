package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.response;

import java.time.LocalDate;
import java.util.List;

public record ReprocessShoppingReceiptResponse(
        String id,
        LocalDate shoppingDate,
        String storeName,
        List<ReprocessShoppingReceiptProductResponse> products,
        List<SuggestedStorageSpotResponse> storageSpots
) {
}
