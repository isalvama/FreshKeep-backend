package com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.response;

import java.time.LocalDateTime;

public record CreateSpaceInvitationResponse(
        String id,
        String token,
        String spaceId,
        String userCreatorId,
        LocalDateTime expiresAt,
        Boolean isActive
) {
}