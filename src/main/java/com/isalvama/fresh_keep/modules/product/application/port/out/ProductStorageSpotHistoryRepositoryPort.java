package com.isalvama.fresh_keep.modules.product.application.port.out;

import com.isalvama.fresh_keep.modules.product.domain.model.Product;

import java.util.List;
import java.util.UUID;

public interface ProductStorageSpotHistoryRepositoryPort {
    void saveAll(List<Product> product, UUID creatorId);
}
