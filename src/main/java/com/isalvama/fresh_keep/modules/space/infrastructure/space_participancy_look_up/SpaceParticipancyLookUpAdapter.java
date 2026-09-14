package com.isalvama.fresh_keep.modules.space.infrastructure.space_participancy_look_up;

import com.isalvama.fresh_keep.modules.product.application.port.out.SpaceParticipancyLookUpPort;
import com.isalvama.fresh_keep.modules.space.application.port.out.SpaceRepositoryPort;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotId;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SpaceParticipancyLookUpAdapter implements SpaceParticipancyLookUpPort {
    private final SpaceRepositoryPort spaceRepositoryPort;

    @Override
    public boolean isParticipant(String userId, String storageSpotId) {
        return spaceRepositoryPort.existsByIdAndParticipantId(UserId.from(userId), StorageSpotId.from(storageSpotId));
    }

    @Override
    public Set<String> filterAccessible(String userId, Set<String> storageSpotIds) {
        List<StorageSpotId> storageIds = storageSpotIds.stream().map(StorageSpotId::from).toList();
        return spaceRepositoryPort.findAccessible(userId, storageIds);
    }
}
