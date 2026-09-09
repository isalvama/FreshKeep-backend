package com.isalvama.fresh_keep.modules.product.application.port.in.command;

import java.util.List;
import java.util.UUID;

public record RegisterProductsCommand(
        UUID creatorId,
        List<ProductsCommand> products
) {
}
