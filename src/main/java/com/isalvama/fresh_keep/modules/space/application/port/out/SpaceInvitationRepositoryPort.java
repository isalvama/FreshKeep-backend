package com.isalvama.fresh_keep.modules.space.application.port.out;

import com.isalvama.fresh_keep.modules.space.domain.model.SpaceInvitation;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.Token;

import java.util.Optional;

public interface SpaceInvitationRepositoryPort {
    void save (SpaceInvitation invitation);
    Optional<SpaceInvitation> findBy (Token token);
}
