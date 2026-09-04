package com.isalvama.fresh_keep.modules.space.infrastructure.space_look_up;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.SpaceLookUpPort;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.GetStorageSpotsDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.StorageSpotDto;
import com.isalvama.fresh_keep.modules.space.domain.exception.NonExistentSpaceException;
import com.isalvama.fresh_keep.modules.space.application.port.out.SpaceRepositoryPort;
import com.isalvama.fresh_keep.modules.space.domain.exception.SpaceNotAvailableForParticipantException;
import com.isalvama.fresh_keep.modules.space.domain.model.Space;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpaceLookUpAdapter implements SpaceLookUpPort {
    private final SpaceRepositoryPort spaceRepositoryPort;

    @Override
    public List<StorageSpotDto> getStorageSpotsBySpaceIdAndParticipantId(GetStorageSpotsDto dto) {
        Space space = spaceRepositoryPort.getById(SpaceId.from(dto.spaceId()))
                .orElseThrow(() -> new NonExistentSpaceException("Space with id " + dto.spaceId() + " does not exist."));

        List<Space> spaces = spaceRepositoryPort.getByParticipantId(UserId.from(dto.creatorId()));

        if (!spaces.contains(space)){
            throw new SpaceNotAvailableForParticipantException("User with id " + dto.creatorId() + " is not a participant of the Space with id " + dto.spaceId());
        }

        return space.getStorageSpots().stream()
                .map(s -> StorageSpotDto.create(s.getId().toString(), s.getName().value(), s.getType().name()))
                .toList();
    }
}
