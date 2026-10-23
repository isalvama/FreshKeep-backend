package com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity.JpaSpaceEntity;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity.JpaStorageSpotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface JpaSpaceSpringDataRepository extends JpaRepository<JpaSpaceEntity, UUID> {
    @Query("""
        SELECT DISTINCT s
        FROM JpaSpaceEntity s
        LEFT JOIN FETCH s.participants
        LEFT JOIN FETCH s.storageSpots
        WHERE s.id = :spaceId
        """)
    Optional<JpaSpaceEntity> findByIdWithRelations(@Param("spaceId") UUID spaceId);

    @Query("""
        SELECT DISTINCT s FROM JpaSpaceEntity s
                JOIN FETCH s.participants p
        LEFT JOIN FETCH s.storageSpots
                        WHERE p.id.participantId = :participantId
        """)
    List<JpaSpaceEntity> findByParticipantId(@Param("participantId") UUID participantId);

    @Query("""
        SELECT COUNT(ss) > 0 FROM JpaStorageSpotEntity ss
                JOIN ss.space s
                        JOIN s.participants p
                        WHERE ss.id = :storageSpotId
                        AND p.id.participantId = :participantId
        """)
    boolean existsStorageSpotByIdAndParticipantId(@Param("participantId") UUID participantId, @Param("storageSpotId") UUID storageSpotId);

    @Query("""
        SELECT COUNT(s) > 0 FROM JpaSpaceEntity s
                JOIN s.participants p
                        WHERE s.id = :spaceId
                        AND p.id.participantId = :participantId
        """)
    boolean existsSpaceByIdAndParticipantId(@Param("participantId") UUID participantId, @Param("spaceId") UUID spaceId);

    @Query("""
        SELECT ss.id FROM JpaStorageSpotEntity ss
                JOIN ss.space s
                        JOIN s.participants p
                        WHERE ss.id IN :ids
                        AND p.id.participantId = :participantId
        """)
    Set<UUID> findAccessible (@Param("participantId") UUID participantId, @Param("ids") List<UUID> storageSpotIds);

    @Query("""
        SELECT ss FROM JpaStorageSpotEntity ss
                WHERE ss.id IN :ids
        """)
    List<JpaStorageSpotEntity> findStorageSpotsByIds (@Param("ids") Set<UUID> storageSpotIds);
}
