package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.mapper;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.*;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.response.*;
import org.springframework.stereotype.Component;

@Component
public class ShoppingReceiptResponseMapper {

    public ProcessNewShoppingReceiptResponse toResponse (ProcessNewShoppingReceiptResult result){
        return new ProcessNewShoppingReceiptResponse(
                result.shoppingReceiptId(),
                result.receiptImageId(),
                result.storageSpotResults().stream().map(this::toSuggestedStorageSpotResponse).toList(),
                result.purchaseShoppingDate(),
                result.storeName(),
                result.productExtractions().stream().map(this::toProductResponse).toList(),
                result.flaggedProducts().stream().map(this::toProductResponse).toList()
                );
    }

    private ProductResponse toProductResponse(ProcessProductResult p){
        return new ProductResponse(
                p.expirationDate(),
                p.productName(),
                p.suggestedStorageSpotId(),
                p.productType(),
                p.priceAmount(),
                p.currency()
        );
    }

    private SuggestedStorageSpotResponse toSuggestedStorageSpotResponse(SuggestedStorageSpotResult ss){
        return new SuggestedStorageSpotResponse(
                ss.id(),
                ss.name(),
                ss.type()
        );
    }

    public ShoppingReceiptResponse toResponse (ShoppingReceiptResult result){
        return new ShoppingReceiptResponse(
                result.shoppingReceiptId(),
                result.shoppingDate(),
                result.storeName(),
                result.products().stream().map(this::toProductResponse).toList(),
                result.storageSpots().stream().map(this::toSuggestedStorageSpotResponse).toList()
                );
    }

    private ShoppingReceiptProductResponse toProductResponse(ProductResult p){
        return new ShoppingReceiptProductResponse(
                p.id(),
                p.productName(),
                p.expirationDate(),
                p.storageSpotId(),
                p.productType(),
                p.priceAmount(),
                p.currency()
        );
    }
}
