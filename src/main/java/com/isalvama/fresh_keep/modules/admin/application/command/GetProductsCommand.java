package com.isalvama.fresh_keep.modules.admin.application.command;

import com.isalvama.fresh_keep.modules.admin.domain.criteria.ProductSortType;

public record GetProductsCommand(
        ProductSortType sort,
        Integer size,
        Integer page,
        String productType
) {
}
