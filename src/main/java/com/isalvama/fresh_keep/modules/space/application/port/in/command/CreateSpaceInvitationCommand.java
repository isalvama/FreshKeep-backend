package com.isalvama.fresh_keep.modules.space.application.port.in.command;

public record CreateSpaceInvitationCommand(
        String spaceId,
        String userId
) {
}