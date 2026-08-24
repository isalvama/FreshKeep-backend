package com.isalvama.fresh_keep.modules.space.application.port.in;

import com.isalvama.fresh_keep.modules.space.application.port.in.dto.SpaceResult;

import java.util.List;

public interface GetSpacesByParticipantIdUseCase {
    List<SpaceResult> execute(String userId);
}
