package com.isalvama.fresh_keep.shared.infrastructure.ai.dto;

import java.time.LocalDate;
import java.util.List;

public record ReceiptExtraction(
        LocalDate purchaseDate,
        String storeName,
        String errorReason,   // null on success
        List<ProductExtraction> productExtractions
) {
}
