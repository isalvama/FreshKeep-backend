package com.isalvama.fresh_keep.modules.space.application.port.in.command;

public record StorageSpotCommand(
        String storageSpotName,
        String storageSpotType
) {
}
