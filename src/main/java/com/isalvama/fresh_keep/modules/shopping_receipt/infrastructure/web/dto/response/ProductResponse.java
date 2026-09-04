package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.response;

import java.time.LocalDate;

public record ProductResponse(
        LocalDate expirationDate,
        String productName,
        String suggestedStorageSpotId,
        String productType,
        Double priceAmount,
        String currency
) {
}