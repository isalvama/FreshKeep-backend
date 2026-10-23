package com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.mapper;

import com.isalvama.fresh_keep.modules.space.application.port.in.command.StorageSpotCommand;
import com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.request.StorageSpotRequest;

public class StorageSpotCommandMapper {
    public static StorageSpotCommand toCommand(StorageSpotRequest request) {
        return new StorageSpotCommand(
                request.name(),
                request.type()
        );
    }
}
