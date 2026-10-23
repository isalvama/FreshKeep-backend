package com.isalvama.fresh_keep.modules.product.application.port.in.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateProductCommand (
        UUID userId,
        UUID productId,
        String name,
        LocalDate expirationDate,
        String productType,
        BigDecimal amount,
        String currency
) {
}