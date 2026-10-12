package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.product_query.projection;

import com.isalvama.fresh_keep.modules.product.domain.model.ProductType;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ProductSummaryProjection (
        UUID id,
        String name,
        LocalDate expirationDate,
        UUID actualStorageSpotId,
        ProductType productType,
        UUID shoppingReceiptId,
        BigDecimal price,
        Currency currency
){}
