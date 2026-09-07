package com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.product.application.port.out.ProductRepositoryPort;
import com.isalvama.fresh_keep.modules.product.domain.model.Product;
import com.isalvama.fresh_keep.modules.product.infrastructure.exception.ProductPersistenceException;
import com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa.entity.JpaProductEntity;
import com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class JpaProductRepositoryAdapter implements ProductRepositoryPort {
    private final ProductMapper mapper;
    private final JpaProductSpringDataRepository jpaProductRepository;

    @Override
    public void saveAll(List<Product> products) {
        try {
            List<JpaProductEntity> productEntities = products.stream().map(mapper::toEntity).toList();
            jpaProductRepository.saveAll(productEntities);
        } catch (DataAccessException e){
            List<String> productsIds = products.stream().map(p -> p.getId().toString()).toList();
            throw new ProductPersistenceException("Failed to save products with ids " + String.join(", ", productsIds) + e.getMessage());
        }

    }
}
