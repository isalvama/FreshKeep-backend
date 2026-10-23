package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProductExtraction;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProcessProductResult(
            LocalDate expirationDate,
            String productName,
            String suggestedStorageSpotId,
            String productType,
            BigDecimal priceAmount,
            String currency
    ) {

    public static ProcessProductResult fromProductExtraction(ProductExtraction p){
        return new ProcessProductResult(
                p.expirationDate(),
                p.productName(),
                p.suggestedStorageSpotId(),
                p.productType(),
                p.priceAmount(),
                p.currency()
        );
    }
}
