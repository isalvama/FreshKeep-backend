package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto;

import java.time.LocalDate;

public record RegisterProductDto(
        String productName,
        LocalDate expirationDate,
        String suggestedStorageSpotId,
        String productType,
        String shoppingReceiptId,
        Double priceAmount,
        String currency
) {
}
