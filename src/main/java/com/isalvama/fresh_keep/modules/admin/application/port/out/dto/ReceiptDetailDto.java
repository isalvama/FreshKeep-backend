package com.isalvama.fresh_keep.modules.admin.application.port.out.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ReceiptDetailDto(
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
        List<ReceiptProductDto> products
) {
}
