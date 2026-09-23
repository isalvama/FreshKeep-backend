package com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response;

public record ProductTypeCountResponse(
        String productType,
        long productCount
) {
}
