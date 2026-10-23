package com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.product.infrastructure.persistence.jpa.entity.JpaProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaProductSpringDataRepository extends JpaRepository<JpaProductEntity, UUID> {
}
