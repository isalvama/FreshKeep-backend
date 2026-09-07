package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

public record ReprocessShoppingReceiptWithFlaggedProducts(
        byte[] imageBytes,
        String mimeType,
        LocalDate purchaseDate,
        List<ProductExtraction> productExtractions,
        List<ProductExtraction> flaggedProductExtractions,
        List<StorageSpotDto> storageSpots,
        Clock clock,
        List<String> productTypes,
        List<String> moneyCurrencies
) {
}