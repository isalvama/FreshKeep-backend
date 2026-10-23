package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RegisteredProductDto(
        UUID id,
        String productName,
        LocalDate expirationDate,
        String suggestedStorageSpotId,
        String productType,
        BigDecimal priceAmount,
        String currency
) {
}
