package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.entity.JpaShoppingReceiptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface JpaSpringDataShoppingReceiptRepository extends JpaRepository<JpaShoppingReceiptEntity, UUID> {
    @Query("""
        SELECT sr.purchaseDate FROM JpaShoppingReceiptEntity sr
        WHERE :shoppingReceiptId = sr.id
        """)
    Instant getShoppingDateById(@Param("shoppingReceiptId") UUID shoppingReceiptId);
}
