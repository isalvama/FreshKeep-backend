package com.isalvama.fresh_keep.modules.space.application.service;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.SpaceNotAccessibleException;
import com.isalvama.fresh_keep.modules.space.application.port.in.CreateSpaceInvitationUseCase;
import com.isalvama.fresh_keep.modules.space.application.port.in.command.CreateSpaceInvitationCommand;
import com.isalvama.fresh_keep.modules.space.application.port.in.dto.CreateSpaceInvitationResult;
import com.isalvama.fresh_keep.modules.space.application.port.out.SpaceInvitationRepositoryPort;
import com.isalvama.fresh_keep.modules.space.application.port.out.SpaceRepositoryPort;
import com.isalvama.fresh_keep.modules.space.domain.model.SpaceInvitation;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
@Transactional
@RequiredArgsConstructor
public class CreateSpaceInvitationService implements CreateSpaceInvitationUseCase {
    private final SpaceRepositoryPort spaceRepositoryPort;
    private final Clock clock;
    private final SpaceInvitationRepositoryPort spaceInvitationRepositoryPort;

    @Override
    public CreateSpaceInvitationResult execute(CreateSpaceInvitationCommand command) {

        boolean isCreatorParticipant = spaceRepositoryPort.existsSpaceByIdAndParticipantId(UserId.from(command.userId()), SpaceId.from(command.spaceId()));

        if (!isCreatorParticipant){
            throw new SpaceNotAccessibleException("User with id " + command.userId() + " is not a participant of the space with id " + command.spaceId());
        }

        SpaceInvitation spaceInvitation = SpaceInvitation.createWithoutMaxCount(
                SpaceId.from(command.spaceId()),
                UserId.from(command.userId()),
                clock
        );

        spaceInvitationRepositoryPort.save(spaceInvitation);

        return new CreateSpaceInvitationResult(
                spaceInvitation.getId().toString(),
                spaceInvitation.getToken().value(),
                spaceInvitation.getSpaceId().toString(),
                spaceInvitation.getUserCreatorId().toString(),
                spaceInvitation.getExpiresAt(),
                spaceInvitation.getIsActive()
        );
    }
}
