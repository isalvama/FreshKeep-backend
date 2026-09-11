package com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.mapper;

import com.isalvama.fresh_keep.modules.space.application.port.in.dto.SpaceProductResult;
import com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.response.SpaceProductResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductResponseMapper {

    public SpaceProductResponse toResponse (SpaceProductResult result){
        return new SpaceProductResponse(
                result.id(),
                result.productName(),
                result.expirationDate(),
                result.storageSpotId(),
                result.productType(),
                result.priceAmount(),
                result.currency()
        );
    }
}
