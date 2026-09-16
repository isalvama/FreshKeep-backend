package com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.product.application.port.out.ProductRepositoryPort;
import com.isalvama.fresh_keep.modules.product.domain.exception.NonExistentProductException;
import com.isalvama.fresh_keep.modules.product.domain.exception.ProductConcurrentlyModifiedException;
import com.isalvama.fresh_keep.modules.product.domain.model.Product;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.ProductId;
import com.isalvama.fresh_keep.modules.product.infrastructure.exception.ProductPersistenceException;
import com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa.entity.JpaProductEntity;
import com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaProductRepositoryAdapter implements ProductRepositoryPort {
    private final ProductMapper mapper;
    private final JpaProductSpringDataRepository jpaProductRepository;

    @Override
    public void saveAll(List<Product> products) {
        try {
            List<JpaProductEntity> productEntities = products.stream().map(mapper::toEntity).toList();
            jpaProductRepository.saveAllAndFlush(productEntities);
        } catch (DataAccessException e){
            List<String> productsIds = products.stream().map(p -> p.getId().toString()).toList();
            throw new ProductPersistenceException("Failed to save products with ids " + String.join(", ", productsIds) + e.getMessage());
        }
    }

    @Override
    public void save(Product product) {
        jpaProductRepository.findById(product.getId().value())
                .map(entity -> {
                            entity.updateProfile(
                                    product.getName().value(),
                                    product.getExpirationDate(),
                                    product.getActualStorageSpotId().value(),
                                    product.getProductType(),
                                    product.getPrice().amount().value(),
                                    product.getPrice().currency()
                            );
                            jpaProductRepository.save(entity);
                            return entity;
                        }
                )
                .orElseGet(() -> jpaProductRepository.save(mapper.toEntity(product)));
    }

    @Override
    public void delete(Product product, Clock clock) {
        try {
            JpaProductEntity entity = jpaProductRepository.findById(product.getId().value()).orElseThrow(() -> new NonExistentProductException(
                    "Product with id " + product.getId() + " does not exist."));
            entity.delete(clock.instant());
            jpaProductRepository.saveAndFlush(entity);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ProductConcurrentlyModifiedException(
                    "Product with id " + product.getId() + " was modified or deleted by someone else in the meantime. Please retry.");
        } catch (DataAccessException e){
            throw new ProductPersistenceException("Failed to delete product with id " + product.getId().toString() + ". " + e.getMessage());
        }
    }

    @Override
    public void deleteAll(List<Product> products, Clock clock) {
        List<UUID> productsIds = products.stream().map(p -> p.getId().value()).toList();
        try {
            List<JpaProductEntity> entities = jpaProductRepository.findAllById(productsIds);
            if (entities.size() != productsIds.size()) {
                throw new ProductConcurrentlyModifiedException(
                        "One or more products with ids " + productsIds + " were modified or deleted by someone else in the meantime. Please retry.");
            }
            entities.forEach(entity -> entity.delete(clock.instant()));
            jpaProductRepository.saveAllAndFlush(entities);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ProductConcurrentlyModifiedException(
                    "One or more products with ids " + productsIds + " were modified or deleted by someone else in the meantime. Please retry.");
        } catch (DataAccessException e){
            throw new ProductPersistenceException("Failed to delete products with ids " + productsIds + ". " + e.getMessage());
        }
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        Optional<JpaProductEntity> jpaEntity = jpaProductRepository.findById(id.value());
        return jpaEntity.map(mapper::toDomain);
    }

    @Override
    public List<Product> findAllById(List<ProductId> ids) {
        List<UUID> productIds = ids.stream().map(ProductId::value).toList();
        List<JpaProductEntity> jpaEntities = jpaProductRepository.findAllById(productIds);
        if (jpaEntities.isEmpty()){
            return List.of();
        }
        return jpaEntities.stream().map(mapper::toDomain).toList();
    }
}
