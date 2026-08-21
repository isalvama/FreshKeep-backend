package com.isalvama.fresh_keep.modules.space.application.port.in.command;

import java.util.Set;

public record CreateSpaceCommand(
        String creatorId,
        String spaceName,
        Set<StorageSpotCommand> storageSpots,
        String emoji
) {
}
