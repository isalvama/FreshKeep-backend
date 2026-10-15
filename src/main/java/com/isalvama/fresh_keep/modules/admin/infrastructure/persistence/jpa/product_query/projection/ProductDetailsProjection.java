package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.product_query.projection;

import com.isalvama.fresh_keep.modules.product.domain.model.ProductType;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.Currency;
import com.isalvama.fresh_keep.modules.space.domain.model.StorageSpotType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProductDetailsProjection(
        UUID id,
        String name,
        LocalDate expirationDate,
        UUID actualStorageSpotId,
        ProductType productType,
        UUID shoppingReceiptId,
        BigDecimal price,
        Currency currency,
        StorageSpotType storageSpotIdType,
        UUID creatorId,
        String creatorUsername,
        String creatorEmail,
        UUID spaceId,
        String spaceName,
        String storeName,
        Instant purchaseDate,
        Instant createdAt
) {
}
