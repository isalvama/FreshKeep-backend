package com.isalvama.fresh_keep.modules.space.application.port.in.command;

public record GetSpaceOverviewCommand(
        String spaceId,
        String userId
) {
}
