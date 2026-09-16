package com.isalvama.fresh_keep.modules.product.infrastructure.web.dto.response;

import java.time.LocalDate;

public record MoveProductResponse (
        String productId,
        String newStorageSpotId,
        LocalDate newExpirationDate
) {
}
