package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto;

import java.time.LocalDate;
import java.util.List;

public record ReviewNewShoppingReceiptDto(
        List<StorageSpotDto> storageSpots,
        List<ProductExtraction> productExtractions,
        LocalDate shoppingDate
) {
}
