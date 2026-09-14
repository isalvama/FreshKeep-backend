package com.isalvama.fresh_keep.modules.product.application.port.in.command;

public record MoveProductCommand(
        String userId,
        String productId,
        String oldStorageSpotId,
        String newStorageSpotId
) {
}
