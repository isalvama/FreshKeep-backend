package com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa.entity.JpaProductStorageSpotHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaProductStorageSpotHistorySpringDataRepository extends JpaRepository<JpaProductStorageSpotHistoryEntity, Long> {

    @Query("""
        SELECT h FROM JpaProductStorageSpotHistoryEntity
                WHERE h.productId = :productId
                        ORDER BY h.changed_at ASC
        """)
    List<JpaProductStorageSpotHistoryEntity> findByProductId(@Param("productId") UUID productId);
}
