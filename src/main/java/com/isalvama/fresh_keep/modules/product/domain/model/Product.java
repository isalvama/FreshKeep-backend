package com.isalvama.fresh_keep.modules.product.domain.model;

import com.isalvama.fresh_keep.modules.product.domain.model.value_object.ProductId;

public class Product {
    private final ProductId productId;

    public Product(ProductId productId) {
        this.productId = productId;
    }
}