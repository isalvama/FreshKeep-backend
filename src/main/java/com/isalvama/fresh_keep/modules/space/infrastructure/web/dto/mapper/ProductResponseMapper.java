package com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.mapper;

import com.isalvama.fresh_keep.modules.product.application.port.in.result.MoveProductResult;
import com.isalvama.fresh_keep.modules.product.application.port.in.result.UpdateProductResult;
import com.isalvama.fresh_keep.modules.product.infrastructure.web.dto.response.MoveProductResponse;
import com.isalvama.fresh_keep.modules.product.infrastructure.web.dto.response.UpdateProductResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductResponseMapper {

    public MoveProductResponse toResponse (MoveProductResult result){
        return new MoveProductResponse(
                result.productId(),
                result.newStorageSpotId(),
                result.newExpirationDate()
        );
    }

    public UpdateProductResponse toResponse (UpdateProductResult result){
        return new UpdateProductResponse(
                result.productId(),
                result.name(),
                result.expirationDate(),
                result.productType(),
                result.amount(),
                result.currency()
        );
    }
}
