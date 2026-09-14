package com.isalvama.fresh_keep.modules.product.application.port.in.result;

public record MoveProductResult(
        String productId,
        String newStorageSpotId
) {
}
