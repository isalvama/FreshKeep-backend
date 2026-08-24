package com.isalvama.fresh_keep.modules.space.application.port.out;

import com.isalvama.fresh_keep.modules.space.domain.model.Space;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;

import java.util.List;

public interface SpaceRepositoryPort {
    void save (Space space);

    List<Space> getByParticipantId(UserId id);
    }
