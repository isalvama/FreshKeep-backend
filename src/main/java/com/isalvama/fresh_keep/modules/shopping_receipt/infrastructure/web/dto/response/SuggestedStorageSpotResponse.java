package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record SuggestedStorageSpotResponse(
        @Schema(description = "Storage spot id") String storageSpotId,
        @Schema(description = "Storage spot name") String storageSpotName,
        @Schema(description = "One of FRIDGE, FREEZER, PANTRY, FRUIT_BOWL, WINE_CELLAR, COUNTERTOP, SHELF") String storageSpotType
) {
}