package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.ShoppingReceiptRepositoryPort;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.ShoppingReceipt;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ShoppingReceiptId;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.exception.ShoppingReceiptPersistenceException;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.mapper.ShoppingReceiptMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Repository
@RequiredArgsConstructor
public class JpaShoppingReceiptRepositoryAdapter implements ShoppingReceiptRepositoryPort {
    private final JpaSpringDataShoppingReceiptRepository jpaRepository;
    private final ShoppingReceiptMapper shoppingReceiptMapper;

    @Override
    public void save(ShoppingReceipt shoppingReceipt) {
        try {
            jpaRepository.saveAndFlush(shoppingReceiptMapper.toEntity(shoppingReceipt));
        } catch (DataAccessException e) {
            throw new ShoppingReceiptPersistenceException("Failed to persist Shopping Receipt with id " + shoppingReceipt.getId().toString() + ": " + e.getMessage());
        }
    }

    @Override
    public LocalDate getShoppingDate(ShoppingReceiptId shoppingReceiptId) {
        Instant shoppingDate = jpaRepository.getShoppingDateById(shoppingReceiptId.value());
        return LocalDate.ofInstant(shoppingDate, ZoneId.systemDefault());
    }
}
