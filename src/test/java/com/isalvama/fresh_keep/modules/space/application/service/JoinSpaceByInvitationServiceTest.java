package com.isalvama.fresh_keep.modules.space.application.service;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.SpaceNotAccessibleException;
import com.isalvama.fresh_keep.modules.space.application.port.in.command.JoinSpaceByInvitationCommand;
import com.isalvama.fresh_keep.modules.space.application.port.in.dto.JoinSpaceByInvitationResult;
import com.isalvama.fresh_keep.modules.space.application.port.out.SpaceInvitationRepositoryPort;
import com.isalvama.fresh_keep.modules.space.application.port.out.SpaceRepositoryPort;
import com.isalvama.fresh_keep.modules.space.domain.exception.ExpiredSpaceInvitationException;
import com.isalvama.fresh_keep.modules.space.domain.exception.NonExistentSpaceInvitationException;
import com.isalvama.fresh_keep.modules.space.domain.model.SpaceInvitation;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.Count;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceInvitationId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.Token;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JoinSpaceByInvitationServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID SPACE_ID = UUID.randomUUID();
    private static final String TOKEN_VALUE = UUID.randomUUID().toString();
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-09-21T12:00:00Z"), ZoneOffset.UTC);

    @Mock
    private SpaceInvitationRepositoryPort spaceInvitationRepositoryPort;

    @Mock
    private SpaceRepositoryPort spaceRepositoryPort;

    private JoinSpaceByInvitationService service;

    @BeforeEach
    void setUp() {
        service = new JoinSpaceByInvitationService(
                spaceInvitationRepositoryPort, spaceRepositoryPort, CLOCK);
    }

    @Test
    void execute_joinsUserAndPersistsInvitationUsage() {
        SpaceInvitation invitation = validInvitation();
        when(spaceInvitationRepositoryPort.findBy(Token.from(TOKEN_VALUE)))
                .thenReturn(Optional.of(invitation));
        when(spaceRepositoryPort.existsSpaceByIdAndParticipantId(
                UserId.of(USER_ID), SpaceId.of(SPACE_ID))).thenReturn(false);

        JoinSpaceByInvitationResult result = service.execute(
                new JoinSpaceByInvitationCommand(USER_ID.toString(), TOKEN_VALUE));

        assertEquals(SPACE_ID.toString(), result.spaceId());
        assertEquals(1, invitation.getUsesCount().value());
        verify(spaceInvitationRepositoryPort).save(invitation);
        verify(spaceRepositoryPort).addParticipant(UserId.of(USER_ID), SpaceId.of(SPACE_ID));
    }

    @Test
    void execute_throwsWhenInvitationDoesNotExist() {
        when(spaceInvitationRepositoryPort.findBy(Token.from(TOKEN_VALUE)))
                .thenReturn(Optional.empty());

        assertThrows(NonExistentSpaceInvitationException.class, () -> service.execute(
                new JoinSpaceByInvitationCommand(USER_ID.toString(), TOKEN_VALUE)));

        verifyNoInteractions(spaceRepositoryPort);
    }

    @Test
    void execute_throwsWhenUserIsAlreadyAParticipant() {
        SpaceInvitation invitation = validInvitation();
        when(spaceInvitationRepositoryPort.findBy(Token.from(TOKEN_VALUE)))
                .thenReturn(Optional.of(invitation));
        when(spaceRepositoryPort.existsSpaceByIdAndParticipantId(
                UserId.of(USER_ID), SpaceId.of(SPACE_ID))).thenReturn(true);

        assertThrows(SpaceNotAccessibleException.class, () -> service.execute(
                new JoinSpaceByInvitationCommand(USER_ID.toString(), TOKEN_VALUE)));

        verify(spaceInvitationRepositoryPort, never()).save(any());
        verify(spaceRepositoryPort, never()).addParticipant(any(), any());
    }

    @Test
    void execute_throwsWhenInvitationHasExpired() {
        SpaceInvitation invitation = SpaceInvitation.reconstitute(
                SpaceInvitationId.create(), Token.from(TOKEN_VALUE), SpaceId.of(SPACE_ID),
                UserId.create(), LocalDateTime.of(2026, 9, 21, 11, 59), true,
                null, Count.of(0));
        when(spaceInvitationRepositoryPort.findBy(Token.from(TOKEN_VALUE)))
                .thenReturn(Optional.of(invitation));
        when(spaceRepositoryPort.existsSpaceByIdAndParticipantId(
                UserId.of(USER_ID), SpaceId.of(SPACE_ID))).thenReturn(false);

        assertThrows(ExpiredSpaceInvitationException.class, () -> service.execute(
                new JoinSpaceByInvitationCommand(USER_ID.toString(), TOKEN_VALUE)));

        verify(spaceInvitationRepositoryPort, never()).save(any());
        verify(spaceRepositoryPort, never()).addParticipant(any(), any());
    }

    private SpaceInvitation validInvitation() {
        return SpaceInvitation.reconstitute(
                SpaceInvitationId.create(), Token.from(TOKEN_VALUE), SpaceId.of(SPACE_ID),
                UserId.create(), LocalDateTime.of(2026, 9, 22, 12, 0), true,
                null, Count.of(0));
    }
}
