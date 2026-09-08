package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.StorageSpotDto;

public record SuggestedStorageSpotResult(
        String id,
        String name,
        String type
) {

    public static SuggestedStorageSpotResult fromStorageSpotDto(StorageSpotDto dto){
        return new SuggestedStorageSpotResult(
                dto.id(),
                dto.name(),
                dto.type()
        );
    }
}
