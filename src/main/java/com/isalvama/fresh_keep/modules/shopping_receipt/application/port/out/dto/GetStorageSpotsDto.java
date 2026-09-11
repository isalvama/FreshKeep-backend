package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto;

public record GetStorageSpotsDto(
        String spaceId,
        String userId
) {
    public static GetStorageSpotsDto create (String spaceId, String userId) {
        return new GetStorageSpotsDto(
                spaceId,
                userId
        );
    }
}
