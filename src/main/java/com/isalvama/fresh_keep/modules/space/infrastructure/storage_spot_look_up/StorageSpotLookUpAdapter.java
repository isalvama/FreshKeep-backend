package com.isalvama.fresh_keep.modules.space.infrastructure.storage_spot_look_up;

import com.isalvama.fresh_keep.modules.product.application.port.out.StorageSpotLookUpPort;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.StorageSpotInfoDto;
import com.isalvama.fresh_keep.modules.space.application.port.out.SpaceRepositoryPort;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StorageSpotLookUpAdapter implements StorageSpotLookUpPort {
    private final SpaceRepositoryPort spaceRepositoryPort;

    @Override
    public List<StorageSpotInfoDto> findByIds(Set<String> storageSpotIds) {
        Set<StorageSpotId> ids = storageSpotIds.stream().map(StorageSpotId::from).collect(Collectors.toSet());
        return spaceRepositoryPort.findStorageSpotsByIds(ids).stream()
                .map(s -> StorageSpotInfoDto.create(s.getId().toString(), s.getName().value(), s.getType().name()))
                .toList();
    }
}
