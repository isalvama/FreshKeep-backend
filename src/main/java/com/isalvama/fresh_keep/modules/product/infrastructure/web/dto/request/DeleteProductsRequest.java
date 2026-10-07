package com.isalvama.fresh_keep.modules.product.infrastructure.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record DeleteProductsRequest(

        @NotNull
        @NotEmpty
        @Schema(description = "Ids of the products to delete; required, non-empty")
        List<UUID> productsIds
) {
}
