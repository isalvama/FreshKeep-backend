package com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.space.application.port.out.SpaceInvitationRepositoryPort;
import com.isalvama.fresh_keep.modules.space.domain.model.SpaceInvitation;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.Token;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.mapper.SpaceInvitationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaSpaceInvitationRepositoryAdapter implements SpaceInvitationRepositoryPort {
    private final JpaSpaceInvitationSpringDataRepository jpaRepository;
    private final SpaceInvitationMapper spaceInvitationMapper;

    @Override
    public void save(SpaceInvitation invitation) {
        jpaRepository.save(spaceInvitationMapper.toEntity(invitation));
    }

    @Override
    public Optional<SpaceInvitation> findBy(Token spaceInvitationToken) {
         return jpaRepository.findByToken(spaceInvitationToken.value()).map(spaceInvitationMapper::toDomain);
    }
}
