package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ShoppingReceiptProductResponse(
        String id,
        String productName,
        LocalDate expirationDate,
        String storageSpotId,
        String productType,
        BigDecimal priceAmount,
        String currency
) {
}
