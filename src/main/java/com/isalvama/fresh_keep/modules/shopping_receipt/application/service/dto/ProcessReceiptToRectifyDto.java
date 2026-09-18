package com.isalvama.fresh_keep.modules.shopping_receipt.application.service.dto;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProductExtraction;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ReceiptExtraction;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

public record ProcessReceiptToRectifyDto(
        LocalDate purchaseDate,
        Clock clock,
        List<ProductExtraction> productExtractions
) {

    public static ProcessReceiptToRectifyDto from (ReceiptExtraction extraction, Clock clock){
        return new ProcessReceiptToRectifyDto(
                extraction.purchaseDate(),
                clock,
                extraction.productExtractions()
        );
    }
}
