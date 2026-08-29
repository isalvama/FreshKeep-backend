package com.isalvama.fresh_keep.modules.space.application.port.out;

import com.isalvama.fresh_keep.modules.space.domain.model.Space;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;

import java.util.List;
import java.util.Optional;

public interface SpaceRepositoryPort {
    void save (Space space);
    List<Space> getByParticipantId(UserId id);
    Optional<Space> getById(SpaceId spaceId);
    }
