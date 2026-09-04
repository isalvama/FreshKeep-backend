package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto;

import java.time.LocalDate;
import java.util.List;

public record ReceiptExtraction(
        LocalDate purchaseDate,
        String storeName,
        String errorReason,   // null on success
        List<ProductExtraction> productExtractions
) {
}
