package com.isalvama.fresh_keep.modules.product.application.port.out;

import com.isalvama.fresh_keep.modules.product.domain.model.Product;

import java.util.List;

public interface ProductRepositoryPort {
    void saveAll(List<Product> product);
}
