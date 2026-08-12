package com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.mapper;

import com.isalvama.fresh_keep.modules.space.domain.model.StorageSpot;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotName;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity.JpaStorageSpotEntity;

public class StorageSpotsMapper {
    public StorageSpot toDomain(JpaStorageSpotEntity jpaEntity) {
        return StorageSpot.reconstitute(
                StorageSpotId.of(jpaEntity.getId()),
                StorageSpotName.from(jpaEntity.getName()),
                jpaEntity.getType());
    }

    public JpaStorageSpotEntity toEntity(StorageSpot domainEntity) {
        return JpaStorageSpotEntity.builder()
                .id(domainEntity.getId().value())
                .name(domainEntity.getName().value())
                .type(domainEntity.getType())
                .build();
    }
}
