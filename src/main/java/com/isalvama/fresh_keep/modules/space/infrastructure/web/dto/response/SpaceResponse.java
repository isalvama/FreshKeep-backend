package com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.response;

import java.util.List;

public record SpaceResponse(
        String id,
        String spaceName,
        List<StorageSpotResponse> storageSpots,
        String creatorId,
        List<String> participantIds
) {
}
