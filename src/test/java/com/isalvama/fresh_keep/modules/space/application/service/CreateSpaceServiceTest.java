package com.isalvama.fresh_keep.modules.space.application.service;

import com.isalvama.fresh_keep.modules.space.application.port.in.command.CreateSpaceCommand;
import com.isalvama.fresh_keep.modules.space.application.port.in.command.StorageSpotCommand;
import com.isalvama.fresh_keep.modules.space.application.port.in.dto.SpaceResult;
import com.isalvama.fresh_keep.modules.space.application.port.out.SpaceRepositoryPort;
import com.isalvama.fresh_keep.modules.space.domain.exception.InvalidEmojiException;
import com.isalvama.fresh_keep.modules.space.domain.exception.InvalidSpaceException;
import com.isalvama.fresh_keep.modules.space.domain.exception.InvalidSpaceNameException;
import com.isalvama.fresh_keep.modules.space.domain.model.Space;
import com.isalvama.fresh_keep.modules.space.infrastructure.exception.SpacePersistenceException;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import com.isalvama.fresh_keep.shared.domain.exception.InvalidIdException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateSpaceServiceTest {

    private static final String CREATOR_ID = UUID.randomUUID().toString();
    private static final String SPACE_NAME = "Kitchen";
    private static final String EMOJI = "🏠";

    @Mock
    private SpaceRepositoryPort spaceRepositoryPort;

    @InjectMocks
    private CreateSpaceService createSpaceService;

    private static List<StorageSpotCommand> oneSpot() {
        return List.of(new StorageSpotCommand("Main Shelf", "SHELF"));
    }

    @Test
    void execute_returnsSpaceResultMatchingThePersistedSpace() {
        CreateSpaceCommand command = new CreateSpaceCommand(CREATOR_ID, SPACE_NAME, oneSpot(), EMOJI);

        SpaceResult result = createSpaceService.execute(command);

        ArgumentCaptor<Space> captor = ArgumentCaptor.forClass(Space.class);
        verify(spaceRepositoryPort, times(1)).save(captor.capture());
        Space savedSpace = captor.getValue();

        assertEquals(savedSpace.getId().toString(), result.id());
        assertEquals(savedSpace.getName().value(), result.name());
        assertEquals(savedSpace.getCreatorId().toString(), result.creatorId());
        assertEquals(CREATOR_ID, result.creatorId());
        assertEquals(1, result.storageSpotResponses().size());
        assertEquals("Main Shelf", result.storageSpotResponses().getFirst().name());
        assertEquals("SHELF", result.storageSpotResponses().getFirst().type());

        Set<String> expectedParticipantIds = savedSpace.getParticipantIds().stream()
                .map(UserId::toString)
                .collect(Collectors.toSet());
        assertEquals(expectedParticipantIds, Set.copyOf(result.participantIds()));
        assertTrue(result.participantIds().contains(CREATOR_ID));
    }

    @Test
    void execute_throwsInvalidSpaceNameExceptionWhenSpaceNameIsInvalid() {
        CreateSpaceCommand command = new CreateSpaceCommand(CREATOR_ID, "", oneSpot(), EMOJI);

        assertThrows(InvalidSpaceNameException.class, () -> createSpaceService.execute(command));

        verifyNoInteractions(spaceRepositoryPort);
    }

    @Test
    void execute_throwsInvalidEmojiExceptionWhenEmojiIsInvalid() {
        CreateSpaceCommand command = new CreateSpaceCommand(CREATOR_ID, SPACE_NAME, oneSpot(), "not-an-emoji");

        assertThrows(InvalidEmojiException.class, () -> createSpaceService.execute(command));

        verifyNoInteractions(spaceRepositoryPort);
    }

    @Test
    void execute_throwsInvalidIdExceptionWhenCreatorIdIsNotAValidUuid() {
        CreateSpaceCommand command = new CreateSpaceCommand("not-a-uuid", SPACE_NAME, oneSpot(), EMOJI);

        assertThrows(InvalidIdException.class, () -> createSpaceService.execute(command));

        verifyNoInteractions(spaceRepositoryPort);
    }

    @Test
    void execute_throwsIllegalArgumentExceptionWhenStorageSpotTypeIsUnknown() {
        List<StorageSpotCommand> invalidTypeSpot = List.of(new StorageSpotCommand("Main Shelf", "NOT_A_REAL_TYPE"));
        CreateSpaceCommand command = new CreateSpaceCommand(CREATOR_ID, SPACE_NAME, invalidTypeSpot, EMOJI);

        assertThrows(IllegalArgumentException.class, () -> createSpaceService.execute(command));

        verifyNoInteractions(spaceRepositoryPort);
    }

    @Test
    void execute_throwsInvalidSpaceExceptionWhenStorageSpotsAreDuplicated() {
        List<StorageSpotCommand> duplicateSpots = List.of(
                new StorageSpotCommand("Main Shelf", "SHELF"),
                new StorageSpotCommand("Main Shelf", "SHELF")
        );
        CreateSpaceCommand command = new CreateSpaceCommand(CREATOR_ID, SPACE_NAME, duplicateSpots, EMOJI);

        assertThrows(InvalidSpaceException.class, () -> createSpaceService.execute(command));

        verifyNoInteractions(spaceRepositoryPort);
    }

    @Test
    void execute_propagatesExceptionWhenPersistenceFails() {
        CreateSpaceCommand command = new CreateSpaceCommand(CREATOR_ID, SPACE_NAME, oneSpot(), EMOJI);
        doThrow(new SpacePersistenceException("Failed to persist space"))
                .when(spaceRepositoryPort).save(any(Space.class));

        assertThrows(SpacePersistenceException.class, () -> createSpaceService.execute(command));

        verify(spaceRepositoryPort, times(1)).save(any(Space.class));
    }
}
