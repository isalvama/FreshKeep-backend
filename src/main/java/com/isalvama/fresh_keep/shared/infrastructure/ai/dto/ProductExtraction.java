package com.isalvama.fresh_keep.shared.infrastructure.ai.dto;

import java.time.LocalDate;

public record ProductExtraction(
        LocalDate expirationDate,
        String productName,
        String suggestedStorageSpotId,
        String productType,
        Double priceAmount,
        String currency
) {
}
