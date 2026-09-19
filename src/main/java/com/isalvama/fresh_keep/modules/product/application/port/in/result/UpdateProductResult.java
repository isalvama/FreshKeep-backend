package com.isalvama.fresh_keep.modules.product.application.port.in.result;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateProductResult(
        String productId,
        String name,
        LocalDate expirationDate,
        String productType,
        BigDecimal amount,
        String currency
) {
}
