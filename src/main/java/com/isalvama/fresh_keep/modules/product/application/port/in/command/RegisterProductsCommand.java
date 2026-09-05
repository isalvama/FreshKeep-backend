package com.isalvama.fresh_keep.modules.product.application.port.in.command;

import java.util.List;

public record RegisterProductsCommand(
        List<ProductsCommand> products
) {
}
