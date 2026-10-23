package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.entity.JpaReceiptImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaSpringDataReceiptImageRepository extends JpaRepository<JpaReceiptImageEntity, UUID> {

}
