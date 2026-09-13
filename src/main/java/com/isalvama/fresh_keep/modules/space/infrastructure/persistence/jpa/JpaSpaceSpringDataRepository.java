package com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity.JpaSpaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaSpaceSpringDataRepository extends JpaRepository<JpaSpaceEntity, UUID> {
    @Query("""
        SELECT DISTINCT s from JpaSpaceEntity s
        LEFT JOIN FETCH s.storageSpots
        WHERE :participantId member of s.participantIds
        """)
    List<JpaSpaceEntity> findByParticipantId(@Param("participantId") UUID participantId);

    @Query("""
        SELECT COUNT(ss) > 0 FROM JpaStorageSpotEntity ss
                JOIN ss.space s
                        WHERE ss.id = :storageSpotId
                        AND :participantId member of s.participantIds
        """)
    boolean existsByIdAndParticipantId(@Param("participantId") UUID participantId, @Param("StorageSpotId") UUID storageSpotId);
}
