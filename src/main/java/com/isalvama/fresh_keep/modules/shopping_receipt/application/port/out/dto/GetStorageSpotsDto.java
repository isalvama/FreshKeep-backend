package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto;

public record GetStorageSpotsDto(
        String spaceId,
        String creatorId
) {
    public static GetStorageSpotsDto create (String spaceId, String creatorId) {
        return new GetStorageSpotsDto(
                spaceId,
                creatorId
        );
    }
}
