package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result;

import java.time.LocalDate;
import java.util.List;

public record ReProcessShoppingReceiptResult(
        String id,
        LocalDate shoppingDate,
        String storeName,
        List<ReProcessShoppingReceiptProductResult> products,
        List<SuggestedStorageSpotResult> storageSpots
) {
}
