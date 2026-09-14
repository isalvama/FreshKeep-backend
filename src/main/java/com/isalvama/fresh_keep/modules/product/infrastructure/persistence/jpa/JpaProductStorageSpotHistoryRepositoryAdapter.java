package com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.product.application.port.out.ProductStorageSpotHistoryRepositoryPort;
import com.isalvama.fresh_keep.modules.product.application.service.dto.ProductMove;
import com.isalvama.fresh_keep.modules.product.domain.model.Product;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.ProductId;
import com.isalvama.fresh_keep.modules.product.infrastructure.exception.ProductStorageSpotHistoryPersistenceException;
import com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa.entity.JpaProductStorageSpotHistoryEntity;
import com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa.mapper.ProductStorageSpotHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JpaProductStorageSpotHistoryRepositoryAdapter implements ProductStorageSpotHistoryRepositoryPort {
    private final ProductStorageSpotHistoryMapper mapper;
    private final JpaProductStorageSpotHistorySpringDataRepository jpaProductStorageSpotHistoryRepository;

    @Override
    public void saveAll(List<Product> products, UUID creatorId) {
        try {
            List<JpaProductStorageSpotHistoryEntity> jpaProductStorageSpotHistoryEntities = products.stream().map(p -> mapper.toEntity(p, creatorId)).toList();
            jpaProductStorageSpotHistoryRepository.saveAllAndFlush(jpaProductStorageSpotHistoryEntities);
        } catch (DataAccessException e) {
            List<String> productsIds = products.stream().map(p -> p.getId().toString()).toList();
            throw new ProductStorageSpotHistoryPersistenceException(
                    "Failed to save Product Storage Spot histories of products with ids " + String.join(", ", productsIds) + e.getMessage());
        }
    }

    @Override
    public List<ProductMove> findByProductId(ProductId productId) {
        List<JpaProductStorageSpotHistoryEntity> entities = jpaProductStorageSpotHistoryRepository.findByProductId(productId.value());
        return entities.stream().map(e -> new ProductMove(
                e.getUserId().toString(),
                e.getNewStorageSpotId().toString(),
                e.getChangedAt(),
                e.getNewExpirationDate()
                )
        ).toList();
    }
}
