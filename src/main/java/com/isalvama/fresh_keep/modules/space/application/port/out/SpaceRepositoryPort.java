package com.isalvama.fresh_keep.modules.space.application.port.out;

import com.isalvama.fresh_keep.modules.space.domain.model.Space;
import com.isalvama.fresh_keep.modules.space.domain.model.StorageSpot;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotId;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SpaceRepositoryPort {
    void save (Space space);
    List<Space> getByParticipantId(UserId id);
    boolean existsStorageSpotByIdAndParticipantId(UserId userId, StorageSpotId storageSpotId);
    boolean existsSpaceByIdAndParticipantId(UserId userId, SpaceId spaceId);
    Set<String> findAccessible(String userId, List<StorageSpotId> storageSpotIds);
    Optional<Space> getById(SpaceId spaceId);
    List<StorageSpot> findStorageSpotsByIds(Set<StorageSpotId> storageSpotIds);
    }
