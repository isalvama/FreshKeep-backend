package com.isalvama.fresh_keep.modules.shopping_receipt.application.service.dto;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProductExtraction;

import java.time.LocalDate;
import java.util.List;

public record ProductsToRectifyDto(
        LocalDate originalPurchaseDate,
        LocalDate rectifiedPurchaseDate,
        List<ProductExtraction> productExtractions
) {
}
