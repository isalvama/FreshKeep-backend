package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
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
@Table(name = "receipt_images")
public class JpaReceiptImageEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Column(name = "assetId", nullable = false, length = 252)
    private String assetId;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}

