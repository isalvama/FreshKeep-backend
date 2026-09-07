package com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa.mapper;

import com.isalvama.fresh_keep.modules.product.domain.model.Product;
import com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa.entity.JpaProductStorageSpotHistoryEntity;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProductStorageSpotHistoryMapper {

    public JpaProductStorageSpotHistoryEntity toEntity(Product product, UUID creatorId){
        return JpaProductStorageSpotHistoryEntity.builder()
                .productId(product.getId().value())
                .userId(creatorId)
                .newExpirationDate(product.getExpirationDate())
                .newStorageSpotId(product.getActualStorageSpotId().value())
                .changedAt(LocalDateTime.now())
                .build();
    }
}
