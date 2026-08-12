package com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "spaces")
public class JpaSpaceEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Column(name = "creator_id", nullable = false, length = 30)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID creatorId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id")
    private Set<JpaStorageSpotEntity> storageSpots;

    @ElementCollection
    @CollectionTable(
            name = "spaces_participants",
            joinColumns = @JoinColumn(name = "space_id")
    )
    @Column(name = "participant_id")
    @JdbcTypeCode(SqlTypes.UUID)
    private Set<UUID> participantIds;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "last_updated_at")
    private Instant lastUpdatedAt;
}