package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.projection;

import java.time.Instant;
import java.util.UUID;

public record ReceiptSummaryProjection(
        UUID id,
        UUID creatorId,
        String creatorEmail,
        String creatorUsername,
        UUID spaceId,
        String spaceName,
        String storeName,
        Instant purchaseDate,
        Instant createdAt,
        Long productCount
) {
}
