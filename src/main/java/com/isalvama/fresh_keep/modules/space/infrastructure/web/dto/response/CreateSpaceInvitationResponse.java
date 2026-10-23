package com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record CreateSpaceInvitationResponse(
        @Schema(description = "Invitation id") String id,
        @Schema(description = "Invitation token to send to the invitee; consumed via POST /invitations/{token}/join") String token,
        @Schema(description = "Id of the space this invitation grants access to") String spaceId,
        @Schema(description = "userId of the account that created this invitation") String userCreatorId,
        @Schema(description = "Server-local LocalDateTime (UTC clock, no offset) 24h after creation") LocalDateTime expiresAt,
        @Schema(description = "true on creation; stays true as the token is used (there's no per-use deactivation)") Boolean isActive
) {
}