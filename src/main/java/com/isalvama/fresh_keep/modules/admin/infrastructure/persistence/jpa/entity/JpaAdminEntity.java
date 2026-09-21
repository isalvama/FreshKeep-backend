package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.entity;

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
@Table(name = "admins")
public class JpaAdminEntity {

    @Id
    @Column(name = "id", unique = true)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Column(name = "account_id", unique = true, nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID accountId;

    @Column(name = "email", unique = true, nullable = false, length = 30)
    private String email;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
