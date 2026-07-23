package com.isalvama.fresh_keep.modules.account.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountSpringDataRepository extends JpaRepository<JpaAccountEntity, UUID> {
    Optional<JpaAccountEntity> findByEmail(String email);
}
