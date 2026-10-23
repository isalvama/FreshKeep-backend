package com.isalvama.fresh_keep.modules.product.application.port.in.result;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RegisteredProductResult(
        UUID id,
        String productName,
        LocalDate expirationDate,
        String suggestedStorageSpotId,
        String productType,
        BigDecimal priceAmount,
        String currency
) {
}
