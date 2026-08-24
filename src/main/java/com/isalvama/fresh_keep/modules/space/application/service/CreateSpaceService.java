package com.isalvama.fresh_keep.modules.space.application.service;

import com.isalvama.fresh_keep.modules.space.application.port.in.CreateSpaceUseCase;
import com.isalvama.fresh_keep.modules.space.application.port.in.command.CreateSpaceCommand;
import com.isalvama.fresh_keep.modules.space.application.port.in.dto.SpaceResult;
import com.isalvama.fresh_keep.modules.space.application.port.in.dto.StorageSpotResult;
import com.isalvama.fresh_keep.modules.space.application.port.out.SpaceRepositoryPort;
import com.isalvama.fresh_keep.modules.space.domain.model.Space;
import com.isalvama.fresh_keep.modules.space.domain.model.StorageSpot;
import com.isalvama.fresh_keep.modules.space.domain.model.StorageSpotType;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.Emoji;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceName;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotName;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreateSpaceService implements CreateSpaceUseCase {
    private final SpaceRepositoryPort spaceRepositoryPort;

    @Override
    public SpaceResult execute(CreateSpaceCommand command) {
        Space space = Space.create(
                SpaceName.from(command.spaceName()),
                Emoji.from(command.emoji()),
                command.storageSpots().stream()
                        .map(s ->
                                StorageSpot.create(StorageSpotName.from(s.storageSpotName()), StorageSpotType.valueOf(s.storageSpotType())))
                        .collect(Collectors.toSet()
                        ),
                UserId.from(command.creatorId())
                );

        spaceRepositoryPort.save(space);

        return SpaceResult.fromDomain(space);
    }
}
