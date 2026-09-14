package com.isalvama.fresh_keep.modules.product.application.port.out.dto;

public record StorageSpotInfoDto(
        String id,
        String name,
        String type
) {
    public static StorageSpotInfoDto create(String id, String name, String type) {
        return new StorageSpotInfoDto(id, name, type);
    }
}
