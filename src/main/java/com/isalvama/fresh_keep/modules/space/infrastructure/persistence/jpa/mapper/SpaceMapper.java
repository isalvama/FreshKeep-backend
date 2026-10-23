package com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.mapper;

import com.isalvama.fresh_keep.modules.space.domain.model.Space;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.Emoji;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceName;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity.JpaSpaceEntity;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity.JpaSpaceParticipantEntity;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity.JpaStorageSpotEntity;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SpaceMapper {
    private final StorageSpotsMapper storageSpotsMapper;
    private final SpaceParticipantMapper spaceParticipantMapper;

    public Space toDomain(JpaSpaceEntity jpaEntity){
        return Space.reconstitute(
                SpaceId.of(jpaEntity.getId()),
                SpaceName.from(jpaEntity.getName()),
                Emoji.from(jpaEntity.getEmoji()),
                jpaEntity.getStorageSpots().stream().map(storageSpotsMapper::toDomain).collect(Collectors.toSet()),
                UserId.of(jpaEntity.getCreatorId()),
                jpaEntity.getParticipants().stream().map(p -> UserId.of(p.getId().getParticipantId())).collect(Collectors.toSet())
        );
    }

    public JpaSpaceEntity toEntity(Space domainEntity, Clock clock){
        JpaSpaceEntity spaceEntity = JpaSpaceEntity.builder()
                        .id(domainEntity.getId().value())
                        .name(domainEntity.getName().value())
                        .emoji(domainEntity.getEmoji().value())
                        .creatorId(domainEntity.getCreatorId().value())
                        .build();

        Set<JpaSpaceParticipantEntity> spaceParticipantEntities = spaceParticipantMapper.toEntities(spaceEntity, domainEntity.getParticipantIds(), clock);
        spaceParticipantEntities.forEach(entity -> spaceEntity.getParticipants().add(entity));

        Set<JpaStorageSpotEntity> spotEntities = domainEntity.getStorageSpots().stream().map(spot -> {
            JpaStorageSpotEntity spotEntity = storageSpotsMapper.toEntity(spot);
            spotEntity.setSpace(spaceEntity);
            return spotEntity;
        }).collect(Collectors.toSet());
        spaceEntity.setStorageSpots(spotEntities);

        return spaceEntity;
    }
}
