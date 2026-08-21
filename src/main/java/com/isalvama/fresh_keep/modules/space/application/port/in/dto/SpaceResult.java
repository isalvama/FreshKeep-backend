package com.isalvama.fresh_keep.modules.space.application.port.in.dto;

import java.util.List;

public record SpaceResult(
        String id,
        String name,
        List<StorageSpotResult> storageSpotResponses,
        String creatorId,
        List<String> participantIds
) {
}
