package com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.request;

import com.isalvama.fresh_keep.modules.admin.domain.criteria.ProductSortType;
import com.isalvama.fresh_keep.modules.product.domain.model.ProductType;
import com.isalvama.fresh_keep.shared.infrastructure.web.validation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.UUID;
import org.springframework.web.bind.annotation.RequestParam;

public record ProductFilterRequest(
        @RequestParam(defaultValue = ProductSortType.DEFAULT_SORT_VALUE)
        @Schema(description = "Sort order; ties are broken by product id", defaultValue = "NAME_ASC")
        ProductSortType sort,

        @Positive @Max(40)
        @Schema(description = "Page size, maximum 40", defaultValue = "30")
        Integer size,

        @Min(0)
        @Schema(description = "1-based page number; missing or 0 means the first page", defaultValue = "0")
        Integer page,

        @RequestParam(required = false)
        @EnumValue(enumClass = ProductType.class, message = "productType is an invalid ProductType")
        @Schema(description = "Optional. Must be a valid ProductType constant name")
        String productType,

        @RequestParam(required = false)
        @UUID
        @Schema(description = "Optional. Only products on shopping receipts this user (users.id) created")
        String creatorId,

        @RequestParam(required = false)
        @UUID
        @Schema(description = "Optional. Only products on this shopping receipt")
        String shoppingReceiptId,

        @RequestParam(required = false)
        @Schema(description = "Accepted by the request model; currently ignored")
        Boolean isDeleted

) {
    public ProductFilterRequest {
        // @RequestParam defaults don't apply to @ModelAttribute records.
        if (sort == null) sort = ProductSortType.NAME_ASC;
        if (size == null) size = 30;
        if (page == null) page = 0;
    }
}
