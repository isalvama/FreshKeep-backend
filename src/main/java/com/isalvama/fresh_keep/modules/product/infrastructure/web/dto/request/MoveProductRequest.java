package com.isalvama.fresh_keep.modules.product.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MoveProductRequest(
        @NotNull
        UUID oldStorageSpotId,
        @NotNull
        UUID newStorageSpotId
) {
}
