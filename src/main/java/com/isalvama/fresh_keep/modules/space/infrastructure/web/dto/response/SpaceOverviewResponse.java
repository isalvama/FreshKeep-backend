package com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record SpaceOverviewResponse(
        @Schema(description = "Space id") String id,
        @Schema(description = "Space name") String name,
        @Schema(description = "Space emoji") String emoji,
        @Schema(description = "This space's storage spots") List<StorageSpotResponse> storageSpots,
        @Schema(description = "Every product currently stored in this space, sorted by expirationDate ascending (soonest-to-expire first)") List<SpaceProductResponse> productResults
) {
}
