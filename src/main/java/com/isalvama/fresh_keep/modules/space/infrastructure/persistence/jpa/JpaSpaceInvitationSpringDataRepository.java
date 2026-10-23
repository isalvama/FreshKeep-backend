package com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity.JpaSpaceInvitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaSpaceInvitationSpringDataRepository extends JpaRepository<JpaSpaceInvitationEntity, UUID> {
    Optional<JpaSpaceInvitationEntity> findByToken(String token);
}
