package com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ProductInfoResponse(
        UUID id,
        String name,
        LocalDate expirationDate,
        UUID actualStorageSpotId,
        String productType,
        UUID shoppingReceiptId,
        BigDecimal price,
        String currency
) {
}
