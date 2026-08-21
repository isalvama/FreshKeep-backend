package com.isalvama.fresh_keep.modules.space.application.port.in.command;

import java.util.List;

public record CreateSpaceCommand(
        String creatorId,
        String spaceName,
        List<StorageSpotCommand> storageSpots,
        String emoji
) {
}
