package com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record SpaceResponse(
        @Schema(description = "Space id") String id,
        @Schema(description = "Space name") String spaceName,
        @Schema(description = "Space emoji") String emoji,
        @Schema(description = "This space's storage spots") List<StorageSpotResponse> storageSpots,
        @Schema(description = "userId of the account that created this space") String creatorId,
        @Schema(description = "userIds of every participant of this space; always includes creatorId") List<String> participantIds
) {
}
