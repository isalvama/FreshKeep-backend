package com.isalvama.fresh_keep.modules.product.application.port.out;

import com.isalvama.fresh_keep.modules.product.domain.model.Product;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.ProductId;

import java.util.List;
import java.util.Optional;

public interface ProductRepositoryPort {
    void saveAll(List<Product> product);
    void delete(Product product);
    void deleteAll(List<Product> products);
    Optional<Product> findById(ProductId id);
    List<Product> findAllById(List<ProductId> ids);
}
