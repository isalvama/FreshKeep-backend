package com.isalvama.fresh_keep.modules.product.application.port.in.command;

import java.time.LocalDate;

public record ProductsCommand(
        String productName,
        LocalDate expirationDate,
        String suggestedStorageSpotId,
        String productType,
        String shoppingReceiptId,
        Double priceAmount,
        String currency
) {
}
