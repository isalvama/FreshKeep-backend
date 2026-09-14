package com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.space.application.port.out.SpaceRepositoryPort;
import com.isalvama.fresh_keep.modules.space.domain.model.Space;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotId;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity.JpaSpaceEntity;
import com.isalvama.fresh_keep.modules.space.infrastructure.exception.SpacePersistenceException;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.mapper.SpaceMapper;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaSpaceRepositoryAdapter implements SpaceRepositoryPort {
    private final JpaSpaceSpringDataRepository spaceSpringDataRepository;
    private final SpaceMapper spaceMapper;

    @Override
    public void save (Space space){
        try {
            spaceSpringDataRepository.save(spaceMapper.toEntity(space));
        } catch (DataAccessException e) {
            throw new SpacePersistenceException(
                    "Failed to persist space with id " + space.getId().toString() + ": " + e.getMessage());
        }
    }

    @Override
    public List<Space> getByParticipantId(UserId id) {
        try {
            List<JpaSpaceEntity> jpaEntities = spaceSpringDataRepository.findByParticipantId(id.value());
        if (jpaEntities.isEmpty()){
            return List.of();
        }
            return jpaEntities.stream().map(spaceMapper::toDomain).toList();
        } catch (DataAccessException e) {
            throw new SpacePersistenceException(
                    "Failed to retrieve data of spaces where user with id " + id + " is participant in: " + e.getMessage());
        }
    }

    @Override
    public boolean existsByIdAndParticipantId(UserId userId, StorageSpotId storageSpotId) {
             return spaceSpringDataRepository.existsByIdAndParticipantId(userId.value(), storageSpotId.value());
    }

    @Override
    public Set<String> findAccessible(String userId, List<StorageSpotId> storageSpotIds) {
        List<UUID> ids = storageSpotIds.stream().map(StorageSpotId::value).toList();
        Set<UUID> resultantIds = spaceSpringDataRepository.findAccessible(UUID.fromString(userId), ids);
        return resultantIds.stream().map(UUID::toString).collect(Collectors.toSet());
    }

    @Override
    public Optional<Space> getById(SpaceId spaceId) {
        return spaceSpringDataRepository.findById(spaceId.value()).map(spaceMapper::toDomain);
    }
}
