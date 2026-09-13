package com.isalvama.fresh_keep.modules.product.application.port.in.command;

public record DeleteProductCommand(
        String userId,
        String productId
) {
}
