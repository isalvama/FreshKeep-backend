package com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity.JpaSpaceParticipantEntity;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity.JpaSpaceParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaSpaceParticipantSpringDataRepository extends JpaRepository<JpaSpaceParticipantEntity, JpaSpaceParticipantId> {
}
