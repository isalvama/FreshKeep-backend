package com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity;

import com.isalvama.fresh_keep.modules.space.domain.model.StorageSpotType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "storage_spots")
public class JpaStorageSpotEntity {
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_spot_type", nullable = false, length = 30)
    private StorageSpotType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id", nullable = false)
    private JpaSpaceEntity space;
}
