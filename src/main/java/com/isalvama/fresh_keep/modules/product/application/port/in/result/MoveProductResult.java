package com.isalvama.fresh_keep.modules.product.application.port.in.result;

import java.time.LocalDate;

public record MoveProductResult(
        String productId,
        String newStorageSpotId,
        LocalDate newExpirationDate
) {
}
