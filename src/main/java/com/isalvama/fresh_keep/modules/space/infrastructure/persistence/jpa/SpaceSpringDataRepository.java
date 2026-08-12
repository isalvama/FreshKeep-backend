package com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity.JpaSpaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpaceSpringDataRepository extends JpaRepository<JpaSpaceEntity, UUID> {
}
