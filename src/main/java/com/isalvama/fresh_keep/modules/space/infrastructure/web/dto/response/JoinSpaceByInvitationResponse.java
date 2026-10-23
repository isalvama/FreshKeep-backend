package com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record JoinSpaceByInvitationResponse(
        @Schema(description = "Id of the space the caller just joined") String spaceId
) {
}
