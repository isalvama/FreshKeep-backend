package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.dto;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProductExtraction;

import java.time.LocalDate;

public record ProductResult(
            LocalDate expirationDate,
            String productName,
            String suggestedStorageSpotId,
            String productType,
            Double priceAmount,
            String currency
    ) {

    public static ProductResult fromProductExtraction(ProductExtraction p){
        return new ProductResult(
                p.expirationDate(),
                p.productName(),
                p.suggestedStorageSpotId(),
                p.productType(),
                p.priceAmount(),
                p.currency()
        );
    }
}
