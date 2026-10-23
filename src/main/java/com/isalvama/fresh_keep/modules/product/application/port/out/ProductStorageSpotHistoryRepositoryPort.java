package com.isalvama.fresh_keep.modules.product.application.port.out;

import com.isalvama.fresh_keep.modules.product.application.service.dto.ProductMove;
import com.isalvama.fresh_keep.modules.product.domain.model.Product;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.ProductId;

import java.util.List;
import java.util.UUID;

public interface ProductStorageSpotHistoryRepositoryPort {
    void saveAll(List<Product> product, UUID creatorId);
    List<ProductMove> findByProductId(ProductId productId);
}
