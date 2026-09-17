package com.isalvama.fresh_keep.modules.shopping_receipt.application.service.dto;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ProductCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProductExtraction;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.ShoppingReceipt;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

public record ConfirmReceiptToRectifyDto(
        LocalDate oldPurchaseDate,
        LocalDate editedPurchaseDate,
        Clock clock,
        List<ProductExtraction> productExtractions
) {

    public static ConfirmReceiptToRectifyDto from (ShoppingReceipt shoppingReceipt, LocalDate editedPurchaseDate, Clock clock, List<ProductCommand> notEditedProducts){
        return new ConfirmReceiptToRectifyDto(
                shoppingReceipt.getPurchaseDate(),
                editedPurchaseDate,
                clock,
                notEditedProducts.stream().map(p -> new ProductExtraction(
                        p.expirationDate(),
                        p.productName(),
                        p.suggestedStorageSpotId(),
                        p.productType(),
                        p.priceAmount(),
                        p.currency()
                )).toList()
        );
    }
}
