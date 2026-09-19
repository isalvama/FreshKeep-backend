package com.isalvama.fresh_keep.modules.product.infrastructure.web.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateProductResponse(
        String productId,
        String name,
        LocalDate expirationDate,
        String productType,
        BigDecimal amount,
        String currency
) {
}
