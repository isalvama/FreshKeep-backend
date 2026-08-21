package com.isalvama.fresh_keep.modules.space.application.port.in;

import com.isalvama.fresh_keep.modules.space.application.port.in.command.CreateSpaceCommand;
import com.isalvama.fresh_keep.modules.space.application.port.in.dto.SpaceResult;

public interface CreateSpaceUseCase {
    SpaceResult execute (CreateSpaceCommand command);
}
