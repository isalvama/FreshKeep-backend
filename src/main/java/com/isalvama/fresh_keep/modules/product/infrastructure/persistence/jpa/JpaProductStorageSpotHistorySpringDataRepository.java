package com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa.entity.JpaProductStorageSpotHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProductStorageSpotHistorySpringDataRepository extends JpaRepository<JpaProductStorageSpotHistoryEntity, Long> {
}
