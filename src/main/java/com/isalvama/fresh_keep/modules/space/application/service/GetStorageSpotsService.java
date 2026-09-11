package com.isalvama.fresh_keep.modules.space.application.service;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.InvalidSpaceReferenceException;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.SpaceNotAccessibleException;
import com.isalvama.fresh_keep.modules.space.application.port.in.GetStorageSpotsUseCase;
import com.isalvama.fresh_keep.modules.space.application.port.in.command.GetStorageSpotsCommand;
import com.isalvama.fresh_keep.modules.space.application.port.in.dto.StorageSpotResult;
import com.isalvama.fresh_keep.modules.space.application.port.out.SpaceRepositoryPort;
import com.isalvama.fresh_keep.modules.space.domain.model.Space;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetStorageSpotsService implements GetStorageSpotsUseCase {
    private final SpaceRepositoryPort spaceRepositoryPort;

    @Override
    public List<StorageSpotResult> execute(GetStorageSpotsCommand command) {
        Space space = spaceRepositoryPort.getById(SpaceId.from(command.spaceId()))
                .orElseThrow(() -> new InvalidSpaceReferenceException("Space with id " + command.spaceId() + " does not exist."));

        List<Space> spaces = spaceRepositoryPort.getByParticipantId(UserId.from(command.userId()));

        if (!spaces.contains(space)){
            throw new SpaceNotAccessibleException("User with id " + command.userId() + " is not a participant of the Space with id " + command.spaceId());
        }

        return space.getStorageSpots().stream()
                .map(s -> new StorageSpotResult(s.getId().toString(), s.getName().value(), s.getType().name()))
                .toList();
    }
}
