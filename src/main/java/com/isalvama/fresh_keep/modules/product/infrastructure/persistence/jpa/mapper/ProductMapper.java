package com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa.mapper;

import com.isalvama.fresh_keep.modules.product.domain.model.Product;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.Money;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.ProductId;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.ProductName;
import com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa.entity.JpaProductEntity;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ShoppingReceiptId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotId;
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

    public Product toDomain(JpaProductEntity entity){
        return Product.reconstitute(
                        ProductId.of(entity.getId()),
                        ProductName.from(entity.getName()),
                entity.getExpirationDate(),
                StorageSpotId.of(entity.getActualStorageSpotId()),
                        entity.getProductType(),
                        ShoppingReceiptId.of(entity.getShoppingReceiptId()),
                entity.getPrice() != null && entity.getCurrency() != null
                ? Money.from(entity.getPrice(), entity.getCurrency())
                        : null
                );
    }
}
