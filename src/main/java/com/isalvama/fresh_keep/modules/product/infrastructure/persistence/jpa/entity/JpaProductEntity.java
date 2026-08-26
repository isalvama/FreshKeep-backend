package com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa.entity;

import com.isalvama.fresh_keep.modules.product.domain.model.ProductType;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotId;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class JpaProductEntity {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Column(name = "storage_spot_id", nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private StorageSpotId storageSpotId;

    @Column(name = "suggested_storage_spot_id", updatable = false, nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private StorageSpotId suggestedStorageSpotId;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 30)
    private ProductType productType;

    @Column(name = "shopping_receipt_id", updatable = false, nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID shoppingReceiptId;

    @Column(name = "price", updatable = false)
    private Double price;
}
