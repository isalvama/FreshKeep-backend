package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa;

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
@Table(name = "receipt_images")
public class JpaReceiptImageEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Column(name = "image_url", nullable = false, length = 252)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopping_receipt_id", nullable = true)
    private JpaShoppingReceiptEntity shoppingReceipt;
}

