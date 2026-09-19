package com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "space_invitation")
public class JpaSpaceInvitationEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Column(name = "token", updatable = false, nullable = false, unique = true)
    private String token;

    @Column(name = "space_id", updatable = false, nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID spaceId;

    @Column(name = "created_by", updatable = false, nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID userCreatorId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", updatable = false, nullable = false)
    private Instant expiresAt;

    @Column(name = "is_active", updatable = false, nullable = false)
    private Boolean isActive;

    @Column(name = "max_uses")
    private Integer maxUses;

    @Column(name = "uses_count", nullable = false)
    private Integer usesCount;
}
