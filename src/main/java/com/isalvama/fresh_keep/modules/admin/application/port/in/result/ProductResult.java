package com.isalvama.fresh_keep.modules.admin.application.port.in.result;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ProductResult(
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
