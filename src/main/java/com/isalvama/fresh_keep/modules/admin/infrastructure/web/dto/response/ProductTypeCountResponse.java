package com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProductTypeCountResponse(
        @Schema(description = "Product type constant name") String productType,
        @Schema(description = "Number of existing (non-deleted) products of this type") long productCount
) {
}
