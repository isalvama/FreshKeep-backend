package com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.space.application.port.out.SpaceRepositoryPort;
import com.isalvama.fresh_keep.modules.space.domain.model.Space;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity.JpaSpaceEntity;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.exception.SpacePersistenceException;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.mapper.SpaceMapper;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}
