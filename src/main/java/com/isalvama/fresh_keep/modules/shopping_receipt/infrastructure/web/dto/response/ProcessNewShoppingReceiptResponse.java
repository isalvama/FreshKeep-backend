package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.response;

import java.time.LocalDate;
import java.util.List;

public record ProcessNewShoppingReceiptResponse(
        String receiptImageId,
        LocalDate purchaseShoppingDate,
        String storeName,
        List<ProductResponse> productExtractions,
        List<ProductResponse> flaggedProducts
)
{
}
