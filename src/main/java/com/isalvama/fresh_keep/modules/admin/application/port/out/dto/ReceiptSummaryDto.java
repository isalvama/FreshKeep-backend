package com.isalvama.fresh_keep.modules.admin.application.port.out.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ReceiptSummaryDto(
        UUID id,
        UUID creatorId,
        UUID spaceId,
        String storeName,
        LocalDate purchaseDate,
        Instant createdAt
) {
}
