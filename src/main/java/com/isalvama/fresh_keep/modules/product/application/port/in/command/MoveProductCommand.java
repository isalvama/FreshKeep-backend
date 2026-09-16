package com.isalvama.fresh_keep.modules.product.application.port.in.command;

import java.util.UUID;

public record MoveProductCommand(
        UUID userId,
        UUID productId,
        UUID oldStorageSpotId,
        UUID newStorageSpotId
) {
}
