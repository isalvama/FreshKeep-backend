package com.isalvama.fresh_keep.modules.space.application.port.out;

import com.isalvama.fresh_keep.modules.space.domain.model.Space;

public interface SpaceRepositoryPort {
    void save (Space space);
    }
