package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.mapper;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ConfirmShoppingReceiptCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ProcessNewShoppingReceiptCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ProductCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ReProcessShoppingReceiptCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.request.ConfirmShoppingReceiptRequest;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.request.ProcessNewShoppingReceiptRequest;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.request.ProductRequest;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.request.ReProcessShoppingReceiptRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShoppingReceiptCommandMapper {
    public ProcessNewShoppingReceiptCommand toProcessNewShoppingReceiptCommand (String spaceId, ProcessNewShoppingReceiptRequest request, String userId){
        return new ProcessNewShoppingReceiptCommand(
                request.file(),
                userId,
                spaceId,
                request.language()
        );
    }

    public ReProcessShoppingReceiptCommand toReProcessShoppingReceiptCommand (String spaceId, ReProcessShoppingReceiptRequest request, String userId){
        return new ReProcessShoppingReceiptCommand(
                request.receiptImageId(),
                spaceId,
                userId,
                request.shoppingDate(),
                request.storeName(),
                toProductCommand(request.flaggedProducts()),
                toProductCommand(request.allProducts()),
                request.language()
                );
    }

    public ConfirmShoppingReceiptCommand toConfirmShoppingReceiptCommand (String spaceId, ConfirmShoppingReceiptRequest request, String userId){
        return new ConfirmShoppingReceiptCommand(
                request.receiptImageId(),
                spaceId,
                userId,
                request.shoppingDate(),
                request.storeName(),
                toProductCommand(request.allProducts())
        );
    }

    public ProductCommand toProductCommand(ProductRequest request){
        return new ProductCommand(
                request.expirationDate(),
                request.productName(),
                request.suggestedStorageSpotId().toString(),
                request.productType(),
                request.priceAmount(),
                request.currency()
        );
    }

    public List<ProductCommand> toProductCommand(List<ProductRequest> requests){
        return requests.stream().map(this::toProductCommand).toList();
    }
}
