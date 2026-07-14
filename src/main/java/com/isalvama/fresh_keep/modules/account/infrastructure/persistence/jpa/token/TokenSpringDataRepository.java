package com.isalvama.fresh_keep.modules.account.infrastructure.persistence.jpa.token;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenSpringDataRepository extends JpaRepository<JpaTokenEntity, String> {
}
