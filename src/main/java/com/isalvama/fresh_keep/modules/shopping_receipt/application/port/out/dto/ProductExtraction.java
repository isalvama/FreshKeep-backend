package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductExtraction(
        LocalDate expirationDate,
        String productName,
        String suggestedStorageSpotId,
        String productType,
        BigDecimal priceAmount,
        String currency
) {
}
