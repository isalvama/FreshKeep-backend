package com.isalvama.fresh_keep.modules.space.application.port.in;

import com.isalvama.fresh_keep.modules.space.application.port.in.command.CreateSpaceInvitationCommand;
import com.isalvama.fresh_keep.modules.space.application.port.in.dto.CreateSpaceInvitationResult;

public interface CreateSpaceInvitationUseCase {
    CreateSpaceInvitationResult execute(CreateSpaceInvitationCommand command);
}