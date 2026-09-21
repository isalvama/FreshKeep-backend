package com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.mapper;

import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity.JpaSpaceEntity;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity.JpaSpaceParticipantEntity;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SpaceParticipantMapper {

    public Set<JpaSpaceParticipantEntity> toEntities (JpaSpaceEntity space, Set<UserId> participantIds, Clock clock){
        return participantIds.stream().map(participantId -> toEntity(space, UserId.of(participantId.value()), clock)).collect(Collectors.toSet());
    }
    public JpaSpaceParticipantEntity toEntity (JpaSpaceEntity space, UserId participantId, Clock clock){
        return new JpaSpaceParticipantEntity(
                space,
                participantId.value(),
                Instant.now(clock)
        );
    }
}
