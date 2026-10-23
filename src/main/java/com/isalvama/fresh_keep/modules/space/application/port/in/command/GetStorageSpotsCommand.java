package com.isalvama.fresh_keep.modules.space.application.port.in.command;

public record GetStorageSpotsCommand(
        String spaceId,
        String userId
) {
}
