package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.RegisteredProductDto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReProcessShoppingReceiptProductResult(
        String id,
        String productName,
        LocalDate expirationDate,
        String storageSpotId,
        String productType,
        BigDecimal priceAmount,
        String currency
) {

    public static ReProcessShoppingReceiptProductResult toResult (RegisteredProductDto dto){
        return new ReProcessShoppingReceiptProductResult(
                dto.id().toString(),
                dto.productName(),
                dto.expirationDate(),
                dto.suggestedStorageSpotId(),
                dto.productType(),
                dto.priceAmount(),
                dto.currency()
        );
    }
}
