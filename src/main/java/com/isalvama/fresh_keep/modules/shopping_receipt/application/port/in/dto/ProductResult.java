package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.dto;

import java.time.LocalDate;

public record ProductResult(
            LocalDate expirationDate,
            String productName,
            String suggestedStorageSpotId,
            String productType,
            Double priceAmount,
            String money
    ) {
}
