package com.isalvama.fresh_keep.modules.product.application.port.out.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ProductQueryDto(
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
