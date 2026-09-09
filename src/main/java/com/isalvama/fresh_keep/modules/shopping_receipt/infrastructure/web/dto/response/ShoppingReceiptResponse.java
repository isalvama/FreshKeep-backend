package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.response;

import java.time.LocalDate;
import java.util.List;

public record ShoppingReceiptResponse(
        String id,
        LocalDate shoppingDate,
        String storeName,
        List<ShoppingReceiptProductResponse> products,
        List<SuggestedStorageSpotResponse> storageSpots
) {
}
