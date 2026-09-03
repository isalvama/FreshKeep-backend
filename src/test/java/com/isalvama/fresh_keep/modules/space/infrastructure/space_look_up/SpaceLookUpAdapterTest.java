package com.isalvama.fresh_keep.modules.space.infrastructure.space_look_up;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.GetStorageSpotsDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.StorageSpotDto;
import com.isalvama.fresh_keep.modules.space.application.port.out.SpaceRepositoryPort;
import com.isalvama.fresh_keep.modules.space.domain.exception.NonExistentSpaceException;
import com.isalvama.fresh_keep.modules.space.domain.exception.SpaceNotAvailableForParticipantException;
import com.isalvama.fresh_keep.modules.space.domain.model.Space;
import com.isalvama.fresh_keep.modules.space.domain.model.StorageSpot;
import com.isalvama.fresh_keep.modules.space.domain.model.StorageSpotType;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.Emoji;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceName;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotName;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpaceLookUpAdapterTest {

    @Mock
    private SpaceRepositoryPort spaceRepositoryPort;

    @InjectMocks
    private SpaceLookUpAdapter spaceLookUpAdapter;

    private final SpaceId spaceId = SpaceId.create();
    private final UserId userId = UserId.create();
    private final GetStorageSpotsDto dto = new GetStorageSpotsDto(spaceId.toString(), userId.toString());

    @Test
    void getStorageSpotsBySpaceIdAndParticipantId_shouldThrowIfStorageSpotsBySpaceIdReturnEmptyOptional() {
        when(spaceRepositoryPort.getById(spaceId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(NonExistentSpaceException.class, () -> spaceLookUpAdapter.getStorageSpotsBySpaceIdAndParticipantId(dto));

        assertTrue(exception.getMessage().contains("does not exist"));
        verify(spaceRepositoryPort).getById(spaceId);
        verify(spaceRepositoryPort, never()).getByParticipantId(userId);
    }

    @Test
    void getStorageSpotsBySpaceIdAndParticipantId_shouldThrowIfUserIsNotAParticipantOfTheSpace() {
        StorageSpot storageSpot = StorageSpot.create(StorageSpotName.from("Fridge"), StorageSpotType.FRIDGE);
        Space space = Space.create(SpaceName.from("Kitchen"), Emoji.from("😀"), Set.of(storageSpot), userId);

        when(spaceRepositoryPort.getById(spaceId)).thenReturn(Optional.of(space));
        when(spaceRepositoryPort.getByParticipantId(userId)).thenReturn(List.of());

        Exception exception = assertThrows(SpaceNotAvailableForParticipantException.class, () -> spaceLookUpAdapter.getStorageSpotsBySpaceIdAndParticipantId(dto));

        assertTrue(exception.getMessage().contains("is not a participant"));
        verify(spaceRepositoryPort).getById(spaceId);
        verify(spaceRepositoryPort).getByParticipantId(userId);
    }

    @Test
    void getStorageSpotsBySpaceIdAndParticipantId_returnsMappedStorageSpotsWhenUserIsAParticipant() {
        StorageSpot fridge = StorageSpot.create(StorageSpotName.from("Fridge"), StorageSpotType.FRIDGE);
        StorageSpot pantry = StorageSpot.create(StorageSpotName.from("Pantry"), StorageSpotType.PANTRY);
        Space space = Space.create(SpaceName.from("Kitchen"), Emoji.from("😀"), Set.of(fridge, pantry), userId);

        when(spaceRepositoryPort.getById(spaceId)).thenReturn(Optional.of(space));
        when(spaceRepositoryPort.getByParticipantId(userId)).thenReturn(List.of(space));

        List<StorageSpotDto> result = spaceLookUpAdapter.getStorageSpotsBySpaceIdAndParticipantId(dto);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(s ->
                s.id().equals(fridge.getId().toString())
                        && s.name().equals("Fridge")
                        && s.type().equals("FRIDGE")));
        assertTrue(result.stream().anyMatch(s ->
                s.id().equals(pantry.getId().toString())
                        && s.name().equals("Pantry")
                        && s.type().equals("PANTRY")));
        verify(spaceRepositoryPort).getById(spaceId);
        verify(spaceRepositoryPort).getByParticipantId(userId);
    }
}
