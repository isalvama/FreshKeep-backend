package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result;

import java.time.LocalDate;
import java.util.List;

public record ProcessNewShoppingReceiptResult(
        String shoppingReceiptId,
        String receiptImageId,
        List<SuggestedStorageSpotResult> storageSpotResults,
        LocalDate purchaseShoppingDate,
        String storeName,
        List<ProcessProductResult> productExtractions,
        List<ProcessProductResult> flaggedProducts
) { }
