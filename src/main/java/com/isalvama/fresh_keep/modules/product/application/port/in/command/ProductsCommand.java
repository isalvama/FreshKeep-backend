package com.isalvama.fresh_keep.modules.product.application.port.in.command;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductsCommand(
        String productName,
        LocalDate expirationDate,
        String suggestedStorageSpotId,
        String productType,
        String shoppingReceiptId,
        BigDecimal priceAmount,
        String currency
) {
}
