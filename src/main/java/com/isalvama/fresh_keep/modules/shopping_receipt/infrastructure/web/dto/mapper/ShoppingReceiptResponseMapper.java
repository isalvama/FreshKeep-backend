package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.mapper;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.*;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.response.*;
import org.springframework.stereotype.Component;

@Component
public class ShoppingReceiptResponseMapper {

    public ProcessNewShoppingReceiptResponse toResponse (ProcessNewShoppingReceiptResult result){
        return new ProcessNewShoppingReceiptResponse(
                result.receiptImageId(),
                result.storageSpotResults().stream().map(this::toSuggestedStorageSpotResponse).toList(),
                result.purchaseShoppingDate(),
                result.storeName(),
                result.productExtractions().stream().map(this::toProductResponse).toList(),
                result.flaggedProducts().stream().map(this::toProductResponse).toList()
                );
    }

    private ProductResponse toProductResponse(ProductResult p){
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

    public ReprocessShoppingReceiptResponse toResponse (ReProcessShoppingReceiptResult result){
        return new ReprocessShoppingReceiptResponse(
                result.shoppingReceiptId(),
                result.shoppingDate(),
                result.storeName(),
                result.products().stream().map(this::toProductResponse).toList(),
                result.storageSpots().stream().map(this::toSuggestedStorageSpotResponse).toList()
                );
    }

    private ReprocessShoppingReceiptProductResponse toProductResponse(ReProcessShoppingReceiptProductResult p){
        return new ReprocessShoppingReceiptProductResponse(
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
