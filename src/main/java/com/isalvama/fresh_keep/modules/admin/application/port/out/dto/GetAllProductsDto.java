package com.isalvama.fresh_keep.modules.admin.application.port.out.dto;

import com.isalvama.fresh_keep.modules.admin.domain.criteria.ProductSortType;
import com.isalvama.fresh_keep.modules.product.domain.model.ProductType;

import java.util.UUID;

public record GetAllProductsDto(
        ProductSortType sortType,
        Integer offset,
        Integer limit,
        ProductType productType,
        UUID creatorId,
        UUID shoppingReceiptId
) {
}
