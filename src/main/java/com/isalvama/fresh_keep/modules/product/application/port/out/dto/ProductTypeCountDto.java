package com.isalvama.fresh_keep.modules.product.application.port.out.dto;

public record ProductTypeCountDto(
        String productType,
        long productCount
) {
}
