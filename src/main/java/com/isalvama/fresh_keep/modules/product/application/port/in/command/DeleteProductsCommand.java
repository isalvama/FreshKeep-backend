package com.isalvama.fresh_keep.modules.product.application.port.in.command;

import java.util.List;
import java.util.UUID;

public record DeleteProductsCommand (
        UUID userId,
        List<UUID> productsIds
){
}
