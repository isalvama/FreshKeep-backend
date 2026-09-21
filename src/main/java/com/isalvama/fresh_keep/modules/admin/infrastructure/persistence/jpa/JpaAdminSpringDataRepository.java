package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.entity.JpaAdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaAdminSpringDataRepository extends JpaRepository<JpaAdminEntity, UUID> {
    Optional<JpaAdminEntity> findByEmail(String email);
    Optional<JpaAdminEntity> findByAccountId(UUID accountId);
}
