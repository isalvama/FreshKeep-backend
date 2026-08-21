package com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.space.application.port.out.SpaceRepositoryPort;
import com.isalvama.fresh_keep.modules.space.domain.model.Space;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.exception.SpacePersistenceException;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.mapper.SpaceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

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
}
