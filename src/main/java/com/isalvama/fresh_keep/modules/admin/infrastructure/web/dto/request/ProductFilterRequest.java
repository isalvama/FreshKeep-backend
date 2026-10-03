package com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.request;

import com.isalvama.fresh_keep.modules.admin.domain.criteria.ProductSortType;
import com.isalvama.fresh_keep.modules.product.domain.model.ProductType;
import com.isalvama.fresh_keep.shared.infrastructure.web.validation.EnumValue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.UUID;
import org.springframework.web.bind.annotation.RequestParam;

public record ProductFilterRequest(
        @RequestParam(defaultValue = ProductSortType.DEFAULT_SORT_VALUE)
        ProductSortType sort,

        @Positive @Max(40)
        Integer size,

        @Min(0)
        Integer page,

        @RequestParam(required = false)
        @EnumValue(enumClass = ProductType.class, message = "productType is an invalid ProductType")
        String productType,

        @RequestParam(required = false)
        @UUID
        String creatorId,

        @RequestParam(required = false)
        @UUID
        String shoppingReceiptId,

        @RequestParam(required = false)
        Boolean isDeleted

) {
    public ProductFilterRequest {
        // @RequestParam defaults don't apply to @ModelAttribute records.
        if (sort == null) sort = ProductSortType.NAME_ASC;
        if (size == null) size = 30;
        if (page == null) page = 0;
    }
}
