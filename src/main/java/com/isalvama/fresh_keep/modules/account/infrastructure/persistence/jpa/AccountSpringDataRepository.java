package com.isalvama.fresh_keep.modules.account.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AccountSpringDataRepository extends JpaRepository<JpaAccountEntity, UUID> {
    Optional<JpaAccountEntity> findByEmail(String email);
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE JpaAccountEntity a SET a.lastLogIn = :now WHERE a.id = :id")
    void updateLastLogIn(@Param("id") UUID id, @Param("now") Instant now);
}
