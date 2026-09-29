package com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ShoppingReceiptDetailResponse(
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
        List<ReceiptProductResponse> products
) {
}
