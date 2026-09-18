package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.entity;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.ShoppingReceiptStatus;
import jakarta.persistence.*;
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
@Table(name = "shopping_receipts")
public class JpaShoppingReceiptEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Column(name = "creator_id", nullable = false, length = 30)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID creatorId;

    @Column(name = "space_id", nullable = false, length = 30)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID spaceId;

    @Column(name = "receipt_image_id", nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID receiptImageId;

    @Column(name = "purchase_date", updatable = false, nullable = false)
    private Instant purchaseDate;

    @Column(name = "store_name", updatable = false)
    private String storeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ShoppingReceiptStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "last_updated_at")
    private Instant lastUpdatedAt;

    public void update (Instant purchaseDate, String storeName, ShoppingReceiptStatus status){
        this.purchaseDate = purchaseDate;
        this.storeName = storeName;
        this.status = status;
    }
}
