package com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.mapper;

import com.isalvama.fresh_keep.modules.space.application.port.in.command.CreateSpaceCommand;
import com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.request.CreateSpaceRequest;
import lombok.RequiredArgsConstructor;

import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CreateSpaceCommandMapper {
    public static CreateSpaceCommand toCommand (CreateSpaceRequest request, String userId){
        return new CreateSpaceCommand(
                        userId,
                        request.spaceName(),
                        request.storageSpots().stream().map(StorageSpotCommandMapper::toCommand).collect(Collectors.toSet()),
                        request.emoji()
                );
    }
}
