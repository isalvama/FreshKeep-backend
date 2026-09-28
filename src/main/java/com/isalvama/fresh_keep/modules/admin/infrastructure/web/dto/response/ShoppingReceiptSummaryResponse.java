package com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ShoppingReceiptSummaryResponse(
        UUID id,
        UUID creatorId,
        UUID spaceId,
        String storeName,
        LocalDate purchaseDate,
        Instant createdAt
) {
}
