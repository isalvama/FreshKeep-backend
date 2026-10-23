package com.isalvama.fresh_keep.modules.admin.application.port.out.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ReceiptSummaryDto(
        UUID id,
        UUID creatorId,
        String creatorEmail,
        String creatorUsername,
        UUID spaceId,
        String spaceName,
        String storeName,
        LocalDate purchaseDate,
        Instant createdAt,
        long productCount
) {
}
