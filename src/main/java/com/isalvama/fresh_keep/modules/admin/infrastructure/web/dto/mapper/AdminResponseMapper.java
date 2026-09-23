package com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.mapper;

import com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response.ProductInfoResponse;
import com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response.ProductTypeCountResponse;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductQueryDto;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductTypeCountDto;
import org.springframework.stereotype.Component;

@Component
public class AdminResponseMapper {

    public ProductInfoResponse toResponse(ProductQueryDto product) {
        return new ProductInfoResponse(
                product.id(),
                product.name(),
                product.expirationDate(),
                product.actualStorageSpotId(),
                product.productType(),
                product.shoppingReceiptId(),
                product.price(),
                product.currency());
    }

    public ProductTypeCountResponse toResponse(ProductTypeCountDto productType) {
        return new ProductTypeCountResponse(productType.productType(), productType.productCount());
    }
}
