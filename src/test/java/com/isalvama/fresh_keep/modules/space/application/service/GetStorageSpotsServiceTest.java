package com.isalvama.fresh_keep.modules.space.application.service;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.InvalidSpaceReferenceException;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.SpaceNotAccessibleException;
import com.isalvama.fresh_keep.modules.space.application.port.in.command.GetStorageSpotsCommand;
import com.isalvama.fresh_keep.modules.space.application.port.in.dto.StorageSpotResult;
import com.isalvama.fresh_keep.modules.space.application.port.out.SpaceRepositoryPort;
import com.isalvama.fresh_keep.modules.space.domain.model.Space;
import com.isalvama.fresh_keep.modules.space.domain.model.StorageSpot;
import com.isalvama.fresh_keep.modules.space.domain.model.StorageSpotType;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.Emoji;
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
class GetStorageSpotsServiceTest {

    @Mock
    private SpaceRepositoryPort spaceRepositoryPort;

    @InjectMocks
    private GetStorageSpotsService service;

    private final UserId userId = UserId.create();
    private final StorageSpot fridge = StorageSpot.create(StorageSpotName.from("Fridge"), StorageSpotType.FRIDGE);
    private final StorageSpot pantry = StorageSpot.create(StorageSpotName.from("Pantry"), StorageSpotType.PANTRY);
    private final Space space = Space.create(SpaceName.from("Kitchen"), Emoji.from("😀"), Set.of(fridge, pantry), userId);
    private final GetStorageSpotsCommand command = new GetStorageSpotsCommand(space.getId().toString(), userId.toString());

    @Test
    void execute_throwsInvalidSpaceReferenceExceptionWhenSpaceDoesNotExist() {
        when(spaceRepositoryPort.getById(any())).thenReturn(Optional.empty());

        Exception exception = assertThrows(InvalidSpaceReferenceException.class, () -> service.execute(command));

        assertTrue(exception.getMessage().contains("does not exist"));
        verify(spaceRepositoryPort, never()).getByParticipantId(any());
    }

    @Test
    void execute_throwsSpaceNotAccessibleExceptionWhenUserIsNotAParticipant() {
        when(spaceRepositoryPort.getById(any())).thenReturn(Optional.of(space));
        when(spaceRepositoryPort.getByParticipantId(any())).thenReturn(List.of());

        Exception exception = assertThrows(SpaceNotAccessibleException.class, () -> service.execute(command));

        assertTrue(exception.getMessage().contains("is not a participant"));
    }

    @Test
    void execute_returnsMappedStorageSpotsWhenUserIsAParticipant() {
        when(spaceRepositoryPort.getById(any())).thenReturn(Optional.of(space));
        when(spaceRepositoryPort.getByParticipantId(any())).thenReturn(List.of(space));

        List<StorageSpotResult> result = service.execute(command);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(s ->
                s.id().equals(fridge.getId().toString()) && s.name().equals("Fridge") && s.type().equals("FRIDGE")));
        assertTrue(result.stream().anyMatch(s ->
                s.id().equals(pantry.getId().toString()) && s.name().equals("Pantry") && s.type().equals("PANTRY")));
    }

    @Test
    void execute_passesTheExpectedIdsToTheRepository() {
        when(spaceRepositoryPort.getById(any())).thenReturn(Optional.of(space));
        when(spaceRepositoryPort.getByParticipantId(any())).thenReturn(List.of(space));

        service.execute(command);

        verify(spaceRepositoryPort).getById(space.getId());
        verify(spaceRepositoryPort).getByParticipantId(userId);
    }
}
