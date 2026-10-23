package com.isalvama.fresh_keep.modules.space.application.service;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.SpaceNotAccessibleException;
import com.isalvama.fresh_keep.modules.space.application.port.in.command.CreateSpaceInvitationCommand;
import com.isalvama.fresh_keep.modules.space.application.port.in.dto.CreateSpaceInvitationResult;
import com.isalvama.fresh_keep.modules.space.application.port.out.SpaceInvitationRepositoryPort;
import com.isalvama.fresh_keep.modules.space.application.port.out.SpaceRepositoryPort;
import com.isalvama.fresh_keep.modules.space.domain.model.SpaceInvitation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateSpaceInvitationServiceTest {

    @Mock
    private SpaceRepositoryPort spaceRepositoryPort;

    @Mock
    private SpaceInvitationRepositoryPort spaceInvitationRepositoryPort;

    private final UUID userId = UUID.randomUUID();
    private final UUID spaceId = UUID.randomUUID();
    private final Clock clock = Clock.fixed(Instant.parse("2026-09-20T12:30:00Z"), ZoneOffset.UTC);
    private CreateSpaceInvitationService service;

    @BeforeEach
    void setUp() {
        service = new CreateSpaceInvitationService(spaceRepositoryPort, clock, spaceInvitationRepositoryPort);
    }

    @Test
    void execute_createsAndPersistsInvitationForSpaceParticipant() {
        when(spaceRepositoryPort.existsSpaceByIdAndParticipantId(any(), any())).thenReturn(true);

        CreateSpaceInvitationResult result = service.execute(
                new CreateSpaceInvitationCommand(spaceId.toString(), userId.toString()));

        ArgumentCaptor<SpaceInvitation> captor = ArgumentCaptor.forClass(SpaceInvitation.class);
        verify(spaceInvitationRepositoryPort).save(captor.capture());
        SpaceInvitation savedInvitation = captor.getValue();

        assertEquals(savedInvitation.getId().toString(), result.id());
        assertEquals(savedInvitation.getToken().value(), result.token());
        assertEquals(spaceId.toString(), result.spaceId());
        assertEquals(userId.toString(), result.userCreatorId());
        assertEquals(LocalDateTime.of(2026, 9, 21, 12, 30), result.expiresAt());
        assertTrue(result.isActive());
        verify(spaceRepositoryPort).existsSpaceByIdAndParticipantId(any(), any());
    }

    @Test
    void execute_throwsWhenUserIsNotAParticipant() {
        when(spaceRepositoryPort.existsSpaceByIdAndParticipantId(any(), any())).thenReturn(false);

        assertThrows(SpaceNotAccessibleException.class, () -> service.execute(
                new CreateSpaceInvitationCommand(spaceId.toString(), userId.toString())));

        verifyNoInteractions(spaceInvitationRepositoryPort);
    }

    @Test
    void execute_doesNotPersistWhenSpaceParticipationLookupFails() {
        doThrow(new IllegalStateException("lookup failed"))
                .when(spaceRepositoryPort).existsSpaceByIdAndParticipantId(any(), any());

        assertThrows(IllegalStateException.class, () -> service.execute(
                new CreateSpaceInvitationCommand(spaceId.toString(), userId.toString())));

        verifyNoInteractions(spaceInvitationRepositoryPort);
    }
}
