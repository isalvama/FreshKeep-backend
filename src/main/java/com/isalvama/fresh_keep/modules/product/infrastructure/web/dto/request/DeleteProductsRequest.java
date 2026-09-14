package com.isalvama.fresh_keep.modules.product.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record DeleteProductsRequest(

        @NotNull
        @NotEmpty
        List<UUID> productsIds
) {
}
