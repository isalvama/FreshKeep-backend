package com.isalvama.fresh_keep.modules.product.infrastructure.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MoveProductRequest(
        @NotNull
        @Schema(description = "Must match the product's current storage spot")
        UUID oldStorageSpotId,
        @NotNull
        @Schema(description = "The destination storage spot")
        UUID newStorageSpotId
) {
}
