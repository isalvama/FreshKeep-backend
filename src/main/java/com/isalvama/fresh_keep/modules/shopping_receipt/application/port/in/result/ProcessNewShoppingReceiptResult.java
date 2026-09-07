package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result;

import java.time.LocalDate;
import java.util.List;

public record ProcessNewShoppingReceiptResult(
        String receiptImageId,
        List<SuggestedStorageSpotResult> storageSpotResults,
        LocalDate purchaseShoppingDate,
        String storeName,
        List<ProductResult> productExtractions,
        List<ProductResult> flaggedProducts
) { }
