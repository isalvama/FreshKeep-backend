package com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.mapper;

import com.isalvama.fresh_keep.modules.space.domain.model.SpaceInvitation;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity.JpaSpaceInvitationEntity;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

@Component
public class SpaceInvitationMapper {

    public JpaSpaceInvitationEntity toEntity (SpaceInvitation spaceInvitation){
        return JpaSpaceInvitationEntity.builder()
                .id(spaceInvitation.getId().value())
                .token(spaceInvitation.getToken().value())
                .spaceId(spaceInvitation.getSpaceId().value())
                .userCreatorId(spaceInvitation.getUserCreatorId().value())
                .expiresAt(spaceInvitation.getExpiresAt().atZone(ZoneOffset.UTC).toInstant())
                .isActive(spaceInvitation.getIsActive())
                .maxUses(spaceInvitation.getMaxUses() == null ? null : spaceInvitation.getMaxUses().value())
                .usesCount(spaceInvitation.getUsesCount().value())
                .build();
    }
}
