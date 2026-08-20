package com.isalvama.fresh_keep.modules.user.infrastructure.persistence.jpa.spring_data_repository;

import com.isalvama.fresh_keep.modules.user.infrastructure.persistence.jpa.entity.JpaUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaUserSpringDataRepository extends JpaRepository<JpaUserEntity, UUID> {
    Optional<JpaUserEntity> findByEmail(String email);
    Optional<JpaUserEntity> findByAccountId(UUID accountId);
}