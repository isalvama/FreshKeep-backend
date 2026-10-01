package com.isalvama.fresh_keep.modules.admin.application.port.in.result;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ReceiptDetailResult(
        UUID id,
        UUID creatorId,
        String creatorUsername,
        String creatorEmail,
        UUID spaceId,
        String spaceName,
        String storeName,
        LocalDate purchaseDate,
        Instant createdAt,
        UUID receiptImageId,
        String receiptImageAssetId,
        String receiptImageMimeType,
        List<ProductResult> products
) {
    public record ProductResult(
            UUID id,
            String name,
            LocalDate expirationDate,
            UUID actualStorageSpotId,
            String productType,
            BigDecimal price,
            String currency,
            boolean deleted
    ) {
    }
}
