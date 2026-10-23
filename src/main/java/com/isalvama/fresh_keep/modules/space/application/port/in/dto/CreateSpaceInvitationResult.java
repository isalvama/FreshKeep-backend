package com.isalvama.fresh_keep.modules.space.application.port.in.dto;

import java.time.LocalDateTime;

public record CreateSpaceInvitationResult(
        String id,
        String token,
        String spaceId,
        String userCreatorId,
        LocalDateTime expiresAt,
        Boolean isActive
) {
}
