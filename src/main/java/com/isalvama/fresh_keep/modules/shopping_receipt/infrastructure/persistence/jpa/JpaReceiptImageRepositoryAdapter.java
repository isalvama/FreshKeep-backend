package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.ReceiptImageRepositoryPort;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.ReceiptImage;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.mapper.ReceiptImageMapper;
import com.isalvama.fresh_keep.modules.space.infrastructure.exception.SpacePersistenceException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaReceiptImageRepositoryAdapter implements ReceiptImageRepositoryPort {
    private final JpaSpringDataReceiptImageRepository jpaRepository;
    private final ReceiptImageMapper mapper;

    @Override
    public void save(ReceiptImage receiptImage) {
        try {
            jpaRepository.save(mapper.toEntity(receiptImage));
        } catch (DataAccessException e) {
            throw new SpacePersistenceException("Failed to persist Receipt Image with id " + receiptImage.getId().toString() + ": " + e.getMessage());
        }
    }
}
