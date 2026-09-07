package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RegisterProductDto(
        String productName,
        LocalDate expirationDate,
        String suggestedStorageSpotId,
        String productType,
        String shoppingReceiptId,
        BigDecimal priceAmount,
        String currency,
        UUID creatorId
) {
}
