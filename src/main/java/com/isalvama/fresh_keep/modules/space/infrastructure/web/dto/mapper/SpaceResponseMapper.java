package com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.mapper;

import com.isalvama.fresh_keep.modules.space.application.port.in.dto.CreateSpaceInvitationResult;
import com.isalvama.fresh_keep.modules.space.application.port.in.dto.GetSpaceOverviewResult;
import com.isalvama.fresh_keep.modules.space.application.port.in.dto.SpaceProductResult;
import com.isalvama.fresh_keep.modules.space.application.port.in.dto.SpaceResult;
import com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.response.CreateSpaceInvitationResponse;
import com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.response.SpaceOverviewResponse;
import com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.response.SpaceProductResponse;
import com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.response.SpaceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpaceResponseMapper {
    private final StorageSpotResponseMapper storageSpotResponseMapper;
    private final ProductResponseMapper productResponseMapper;

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

    public SpaceOverviewResponse toResponse(GetSpaceOverviewResult result){
        return new SpaceOverviewResponse(
                result.id(),
                result.name(),
                result.emoji(),
                result.storageSpots().stream().map(storageSpotResponseMapper::toResponse).toList(),
                result.productResults().stream().map(this::toResponse).toList()
        );
    }

    public SpaceProductResponse toResponse (SpaceProductResult result){
        return new SpaceProductResponse(
                result.id(),
                result.productName(),
                result.expirationDate(),
                result.storageSpotId(),
                result.productType(),
                result.priceAmount(),
                result.currency()
        );
    }

    public CreateSpaceInvitationResponse toResponse (CreateSpaceInvitationResult result){
        return new CreateSpaceInvitationResponse(
                result.id(),
                result.token(),
                result.spaceId(),
                result.userCreatorId(),
                result.expiresAt(),
                result.isActive()
        );
    }
}
