package com.isalvama.fresh_keep.modules.space.application.port.out;

import com.isalvama.fresh_keep.modules.space.domain.model.SpaceInvitation;

public interface SpaceInvitationRepositoryPort {
    void save (SpaceInvitation invitation);
}
