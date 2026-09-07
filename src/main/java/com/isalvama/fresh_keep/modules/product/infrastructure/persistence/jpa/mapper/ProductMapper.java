package com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa.mapper;

import com.isalvama.fresh_keep.modules.product.domain.model.Product;
import com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa.entity.JpaProductEntity;
import org.springframework.stereotype.Component;


@Component
public class ProductMapper {

    public JpaProductEntity toEntity(Product product){
        return JpaProductEntity.builder()
                .id(product.getId().value())
                .name(product.getName().value())
                .expirationDate(product.getExpirationDate())
                .suggestedStorageSpotId(product.getSuggestedStorageSpotId().value())
                .actualStorageSpotId(product.getActualStorageSpotId().value())
                .productType(product.getProductType())
                .shoppingReceiptId(product.getShoppingReceiptId().value())
                .price(product.getPrice().amount().value())
                .currency(product.getPrice().currency())
                .build();
    }
}
