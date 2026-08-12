package com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.space.application.port.out.SpaceRepositoryPort;
import com.isalvama.fresh_keep.modules.space.domain.model.Space;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.mapper.SpaceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaSpaceRepositoryAdapter implements SpaceRepositoryPort {
    private final SpaceSpringDataRepository spaceSpringDataRepository;
    private final SpaceMapper spaceMapper;

    @Override
    public void save (Space space){
        spaceSpringDataRepository.save(spaceMapper.toEntity(space));
    }
}
