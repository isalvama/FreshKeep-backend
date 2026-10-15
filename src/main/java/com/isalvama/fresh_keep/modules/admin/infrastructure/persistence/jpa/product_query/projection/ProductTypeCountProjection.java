package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.product_query.projection;

import com.isalvama.fresh_keep.modules.product.domain.model.ProductType;

public record ProductTypeCountProjection(
        ProductType productType,
        long productCount
) {
}
