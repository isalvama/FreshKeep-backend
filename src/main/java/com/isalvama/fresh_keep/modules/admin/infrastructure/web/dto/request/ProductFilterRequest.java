package com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.request;

import com.isalvama.fresh_keep.modules.admin.domain.criteria.ProductSortType;
import com.isalvama.fresh_keep.modules.product.domain.model.ProductType;
import com.isalvama.fresh_keep.shared.infrastructure.web.validation.EnumValue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.RequestParam;

public record ProductFilterRequest(
        @RequestParam(defaultValue = ProductSortType.DEFAULT_SORT_VALUE)
        ProductSortType sort,

        @Positive @Max(100)
        Integer size,

        @Min(0)
        Integer page,

        @RequestParam(required = false)
        @EnumValue(enumClass = ProductType.class, message = "Type is an invalid ProductType")
        String productType
) {
    public ProductFilterRequest {
        if (size == null) size = 30;
        if (page == null) page = 0;
    }
}
