package com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "product_storage_spot_history")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JpaProductStorageSpotHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "history_seq")
    @SequenceGenerator(
            name = "history_seq",
            sequenceName = "product_storage_spot_history_seq",
            allocationSize = 30
    )
    private Long id;

    @Column(name = "product_id", nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID productId;

    @Column(name = "user_id", updatable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID userId;

    @Column(name = "new_expiration_date", nullable = false, updatable = false)
    private LocalDate newExpirationDate;

    @Column(name = "new_storage_spot_id", updatable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID newStorageSpotId;

    @Column(name = "changed_at", updatable = false)
    private LocalDateTime changedAt;
}
