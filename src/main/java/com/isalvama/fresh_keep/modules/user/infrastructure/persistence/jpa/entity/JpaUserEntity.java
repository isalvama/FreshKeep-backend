package com.isalvama.fresh_keep.modules.user.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class JpaUserEntity {

    @Id
    @Column(name = "id", unique = true)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Column(name = "account_id", unique = true, nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID accountId;

    @Column(name = "email", unique = true, nullable = false, length = 30)
    private String email;

    @Column(name = "username", unique = true, length = 20)
    private String userName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "last_updated_at", nullable = false)
    private Instant lastUpdatedAt;
}
