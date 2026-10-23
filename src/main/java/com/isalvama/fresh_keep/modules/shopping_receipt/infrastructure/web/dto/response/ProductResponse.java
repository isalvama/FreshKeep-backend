package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductResponse(
        @Schema(description = "Extracted expiration date") LocalDate expirationDate,
        @Schema(description = "Extracted product name, written in the requested language") String productName,
        @Schema(description = "Suggested storage spot id; backfilled with a same-type fallback (or null) if the AI's suggestion isn't one of this space's actual spots") String suggestedStorageSpotId,
        @Schema(description = "Extracted product type constant name") String productType,
        @Schema(description = "Extracted price, if read off the receipt") BigDecimal priceAmount,
        @Schema(description = "Extracted currency, if read off the receipt") String currency
) {
}