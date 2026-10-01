package com.isalvama.fresh_keep.modules.admin.application.port.out.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ReceiptProductDto(
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
