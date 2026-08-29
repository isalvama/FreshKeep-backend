package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto;

public record StorageSpotDto(
        String id,
        String name,
        String type
) {
    public static StorageSpotDto create(String id, String name, String type){
        return new StorageSpotDto(
                id,
                name,
                type
        );
    }
}
