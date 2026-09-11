package com.isalvama.fresh_keep.modules.space.application.port.out.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SpaceProductDto(
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
