package com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.mapper;

import com.isalvama.fresh_keep.modules.space.application.port.in.dto.StorageSpotResult;
import com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.response.StorageSpotResponse;
import org.springframework.stereotype.Component;

@Component
public class StorageSpotResponseMapper {
    public StorageSpotResponse toResponse (StorageSpotResult storageSpotResult){
        return new StorageSpotResponse(
                storageSpotResult.id(),
                storageSpotResult.name(),
                storageSpotResult.type()
        );
    }
}
