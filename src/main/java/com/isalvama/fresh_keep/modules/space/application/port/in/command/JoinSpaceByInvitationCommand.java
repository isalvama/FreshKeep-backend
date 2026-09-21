package com.isalvama.fresh_keep.modules.space.application.port.in.command;

public record JoinSpaceByInvitationCommand(
        String userId,
        String token
) {
}
