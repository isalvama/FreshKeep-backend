package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.dto;

import java.time.LocalDate;
import java.util.List;

public record ProcessNewShoppingReceiptResult(
        LocalDate purchaseShoppingDate,
        String storeName,
        List<ProductResult> productExtractions
) {
}
