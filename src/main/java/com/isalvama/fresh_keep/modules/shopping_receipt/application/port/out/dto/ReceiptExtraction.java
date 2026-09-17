package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.service.dto.RectifiedReceiptDto;

import java.time.LocalDate;
import java.util.List;

public record ReceiptExtraction(
        LocalDate purchaseDate,
        String storeName,
        String errorReason,   // null on success
        List<ProductExtraction> productExtractions
) {
    public static ReceiptExtraction from (RectifiedReceiptDto dto, String storeName, String errorReason){
        return new ReceiptExtraction(
                dto.purchaseDate(),
                storeName,
                errorReason,
                dto.productExtractions()
        );
    }
}
