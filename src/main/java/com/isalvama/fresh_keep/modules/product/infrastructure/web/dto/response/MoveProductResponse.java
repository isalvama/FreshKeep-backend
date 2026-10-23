package com.isalvama.fresh_keep.modules.product.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record MoveProductResponse (
        @Schema(description = "Product id") String productId,
        @Schema(description = "Id of the storage spot the product was moved to") String newStorageSpotId,
        @Schema(description = "Recalculated expiration date after the move; may differ from the product's previous date") LocalDate newExpirationDate
) {
}
