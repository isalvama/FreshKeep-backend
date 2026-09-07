package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.mapper;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.ProcessNewShoppingReceiptResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.ProductResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.SuggestedStorageSpotResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.response.ProcessNewShoppingReceiptResponse;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.response.ProductResponse;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.response.SuggestedStorageSpotResponse;
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
}
