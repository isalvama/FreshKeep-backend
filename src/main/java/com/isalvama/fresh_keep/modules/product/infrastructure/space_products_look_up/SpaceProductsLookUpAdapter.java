package com.isalvama.fresh_keep.modules.product.infrastructure.space_products_look_up;

import com.isalvama.fresh_keep.modules.product.application.port.out.ProductQueryPort;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductQueryDto;
import com.isalvama.fresh_keep.modules.space.application.port.out.SpaceProductsLookUpPort;
import com.isalvama.fresh_keep.modules.space.application.port.out.dto.SpaceProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SpaceProductsLookUpAdapter implements SpaceProductsLookUpPort {
    private final ProductQueryPort productQueryPort;

    @Override
    public List<SpaceProductDto> getProductsFromSpaceId(UUID spaceId) {

        List<ProductQueryDto> productQueryDtos = productQueryPort.getSpaceProducts(spaceId);

        if (productQueryDtos.isEmpty()){
            return List.of();
        }

        return productQueryDtos.stream().map(p -> new SpaceProductDto(
                        p.id(),
                        p.name(),
                        p.expirationDate(),
                        p.actualStorageSpotId(),
                        p.productType(),
                        p.shoppingReceiptId(),
                        p.price(),
                        p.currency()
                )).toList();
    }
}