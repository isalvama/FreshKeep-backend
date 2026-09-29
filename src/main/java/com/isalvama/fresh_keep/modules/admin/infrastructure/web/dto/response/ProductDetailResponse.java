package com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProductDetailResponse(
        UUID id,
        String name,
        String productType,
        LocalDate expirationDate,
        UUID actualStorageSpotId,
        String storageSpotIdType,
        UUID creatorId,
        String creatorUsername,
        String creatorEmail,
        UUID spaceId,
        String spaceName,
        String storeName,
        LocalDate purchaseDate,
        Instant createdAt,
        UUID shoppingReceiptId,
        BigDecimal price,
        String currency
) {
}
