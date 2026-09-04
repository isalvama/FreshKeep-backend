package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.exception;

import com.isalvama.fresh_keep.shared.infrastructure.exception.InfrastructureException;

public class ReceiptImagePersistenceException extends InfrastructureException {
    public ReceiptImagePersistenceException(String message) {
        super(message);
    }
}
