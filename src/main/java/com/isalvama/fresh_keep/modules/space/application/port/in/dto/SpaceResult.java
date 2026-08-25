package com.isalvama.fresh_keep.modules.space.application.port.in.dto;

import com.isalvama.fresh_keep.modules.space.domain.model.Space;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;

import java.util.List;

public record SpaceResult(
        String id,
        String name,
        String emoji,
        List<StorageSpotResult> storageSpotResponses,
        String creatorId,
        List<String> participantIds
) {

    public static SpaceResult constitute (String id, String name, String emoji, List<StorageSpotResult> storageSpotResponses, String creatorId, List<String> participantIds
    ){
        return new SpaceResult(
                id,
                name,
                emoji,
                storageSpotResponses,
                creatorId,
                participantIds
        );
    }

    public static SpaceResult fromDomain (Space space){
        return SpaceResult.constitute(
                space.getId().toString(),
                space.getName().value(),
                space.getEmoji().value(),
                space.getStorageSpots().stream().map(s ->
                        StorageSpotResult.constitute(
                                s.getId().toString(),
                                s.getName().value(),
                                s.getType().toString()
                        )
                ).toList(),
                space.getCreatorId().toString(),
                space.getParticipantIds().stream().map(UserId::toString).toList()
        );
    }
}
