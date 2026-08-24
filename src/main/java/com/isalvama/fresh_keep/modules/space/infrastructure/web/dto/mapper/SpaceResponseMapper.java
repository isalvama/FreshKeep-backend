package com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.mapper;

import com.isalvama.fresh_keep.modules.space.application.port.in.dto.SpaceResult;
import com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.response.SpaceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpaceResponseMapper {
    private final StorageSpotResponseMapper storageSpotResponseMapper;

    public SpaceResponse toResponse(SpaceResult spaceResult){
        return new SpaceResponse(
                spaceResult.id(),
                spaceResult.name(),
                spaceResult.emoji(),
                spaceResult.storageSpotResponses().stream().map(storageSpotResponseMapper::toResponse).toList(),
                spaceResult.creatorId(),
                spaceResult.participantIds()
        );
    }
}
