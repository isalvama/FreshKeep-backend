package com.isalvama.fresh_keep.modules.space.application.service;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.SpaceNotAccessibleException;
import com.isalvama.fresh_keep.modules.space.application.port.in.JoinSpaceByInvitationUseCase;
import com.isalvama.fresh_keep.modules.space.application.port.in.command.JoinSpaceByInvitationCommand;
import com.isalvama.fresh_keep.modules.space.application.port.in.dto.JoinSpaceByInvitationResult;
import com.isalvama.fresh_keep.modules.space.application.port.out.SpaceInvitationRepositoryPort;
import com.isalvama.fresh_keep.modules.space.application.port.out.SpaceRepositoryPort;
import com.isalvama.fresh_keep.modules.space.domain.exception.NonExistentSpaceInvitationException;
import com.isalvama.fresh_keep.modules.space.domain.model.SpaceInvitation;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.Token;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
@RequiredArgsConstructor
@Transactional
public class JoinSpaceByInvitationService implements JoinSpaceByInvitationUseCase {
    private final SpaceInvitationRepositoryPort spaceInvitationRepositoryPort;
    private final SpaceRepositoryPort spaceRepositoryPort;
    private final Clock clock;

    @Override
    public JoinSpaceByInvitationResult execute(JoinSpaceByInvitationCommand command) {
        SpaceInvitation invitation = spaceInvitationRepositoryPort.findBy(Token.from(command.token()))
                .orElseThrow(() -> new NonExistentSpaceInvitationException("Space Invitation with token" + command.token() + " was not found."));

        boolean isParticipant = spaceRepositoryPort.existsSpaceByIdAndParticipantId(UserId.from(command.userId()), invitation.getSpaceId());

        if (isParticipant){
            throw new SpaceNotAccessibleException("User with id " + command.userId() + " cannot join the space with id " + invitation.getSpaceId() + " as he/she is already a participant");
        }

        invitation.use(clock);
        spaceInvitationRepositoryPort.save(invitation);
        spaceRepositoryPort.addParticipant(UserId.from(command.userId()), invitation.getSpaceId());

        return new JoinSpaceByInvitationResult(
                invitation.getSpaceId().toString()
        );
    }
}
