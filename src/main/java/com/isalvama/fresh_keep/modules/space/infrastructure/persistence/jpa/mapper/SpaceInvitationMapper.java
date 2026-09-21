package com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.mapper;

import com.isalvama.fresh_keep.modules.space.domain.model.SpaceInvitation;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.Count;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceInvitationId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.Token;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity.JpaSpaceInvitationEntity;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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

    public SpaceInvitation toDomain (JpaSpaceInvitationEntity entity){
        return SpaceInvitation.reconstitute(
                SpaceInvitationId.of(entity.getId()),
                Token.from(entity.getToken()),
                SpaceId.of(entity.getSpaceId()),
                UserId.of(entity.getUserCreatorId()),
                LocalDateTime.ofInstant(entity.getExpiresAt(), ZoneOffset.UTC),
                entity.getIsActive(),
                entity.getMaxUses() != null ? Count.of(entity.getMaxUses()) : null,
                Count.of(entity.getUsesCount())
        );
    }
}
