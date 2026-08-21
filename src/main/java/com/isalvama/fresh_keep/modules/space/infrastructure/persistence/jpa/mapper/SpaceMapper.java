package com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.mapper;

import com.isalvama.fresh_keep.modules.space.domain.model.Space;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceName;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity.JpaSpaceEntity;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity.JpaStorageSpotEntity;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SpaceMapper {
    private final StorageSpotsMapper storageSpotsMapper;

    public Space toDomain(JpaSpaceEntity jpaEntity){
        return Space.reconstitute(
                SpaceId.of(jpaEntity.getId()),
                SpaceName.from(jpaEntity.getName()),
                jpaEntity.getStorageSpots().stream().map(storageSpotsMapper::toDomain).collect(Collectors.toSet()),
                UserId.of(jpaEntity.getCreatorId()),
                jpaEntity.getParticipantIds().stream().map(UserId::of).collect(Collectors.toSet())
        );
    }

    public JpaSpaceEntity toEntity(Space domainEntity){
        JpaSpaceEntity spaceEntity = JpaSpaceEntity.builder().
                id(domainEntity.getId().value())
                        .name(domainEntity.getName().value())
                                .creatorId(domainEntity.getCreatorId().value())
                .participantIds(domainEntity.getParticipantIds().stream().map(UserId::value).collect(Collectors.toSet())).build();

        Set<JpaStorageSpotEntity> spotEntities = domainEntity.getStorageSpots().stream().map(spot -> {
            JpaStorageSpotEntity spotEntity = storageSpotsMapper.toEntity(spot);
            spotEntity.setSpace(spaceEntity);
            return spotEntity;
        })
                .collect(Collectors.toSet());
        spaceEntity.setStorageSpots(spotEntities);
        return spaceEntity;
    }
}
