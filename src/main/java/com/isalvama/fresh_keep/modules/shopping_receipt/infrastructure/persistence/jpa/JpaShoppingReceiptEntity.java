package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.product.domain.model.value_object.ProductId;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.ReceiptImage;
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

    @OneToMany(mappedBy = "shoppingReceipt", orphanRemoval = false, fetch = FetchType.LAZY)
    private ReceiptImage receipt;

    @Column(name = "purchase_date", updatable = false, nullable = false)
    private Instant purchaseDate;

    @Column(name = "store_name", updatable = false)
    private String storeName;

    @ElementCollection
    @CollectionTable(
            name = "products",
            joinColumns = @JoinColumn(name = "shopping_receipt_id")
    )
    @Column(name = "id")
    @JdbcTypeCode(SqlTypes.UUID)
    private Set<UUID> productIds;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "last_updated_at")
    private Instant lastUpdatedAt;
}
