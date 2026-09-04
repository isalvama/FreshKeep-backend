package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.mapper;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.dto.ProcessNewShoppingReceiptResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.dto.ProductResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.response.ProcessNewShoppingReceiptResponse;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.response.ProductResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShoppingReceiptResponseMapper {

    public ProcessNewShoppingReceiptResponse toResponse (ProcessNewShoppingReceiptResult result){
        return new ProcessNewShoppingReceiptResponse(
                result.receiptImageId(),
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
}
