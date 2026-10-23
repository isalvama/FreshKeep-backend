package com.isalvama.fresh_keep.modules.space.application.port.in;

import com.isalvama.fresh_keep.modules.space.application.port.in.command.JoinSpaceByInvitationCommand;
import com.isalvama.fresh_keep.modules.space.application.port.in.dto.JoinSpaceByInvitationResult;

public interface JoinSpaceByInvitationUseCase {
    JoinSpaceByInvitationResult execute (JoinSpaceByInvitationCommand command);
}
