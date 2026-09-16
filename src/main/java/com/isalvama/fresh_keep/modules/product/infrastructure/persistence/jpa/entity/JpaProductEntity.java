package com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa.entity;

import com.isalvama.fresh_keep.modules.product.domain.model.ProductType;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.Currency;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
@SQLRestriction("deleted_at IS NULL")
public class JpaProductEntity implements Persistable<UUID> {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Column(name = "suggested_storage_spot_id", updatable = false, nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID suggestedStorageSpotId;

    @Column(name = "actual_storage_spot_id", nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID actualStorageSpotId;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 30)
    private ProductType productType;

    @Column(name = "shopping_receipt_id", updatable = false, nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID shoppingReceiptId;

    @Column(name = "price", updatable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", updatable = false, length = 20)
    private Currency currency;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "last_updated_at")
    private Instant lastUpdatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    private Long version;

    @Transient
    private boolean isNew = true; // Defaults to true for new instances

    @Override
    public boolean isNew() {
        return this.isNew;
    }

    // A lifecycle callback to change the flag after Hibernate reads it from the DB
    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdatedAt = Instant.now();
    }

    public void updateProfile(String name, LocalDate expirationDate, UUID actualStorageSpotId, ProductType productType, BigDecimal price, Currency currency){
        this.name = name;
        this.expirationDate = expirationDate;
        this.actualStorageSpotId = actualStorageSpotId;
        this.productType = productType;
        this.price = price;
        this.currency = currency;
    }

    public void delete(Instant deletedAt){
        this.deletedAt = deletedAt;
    }

}
