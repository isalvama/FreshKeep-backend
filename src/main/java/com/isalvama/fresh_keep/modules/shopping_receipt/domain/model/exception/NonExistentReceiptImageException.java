package com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class NonExistentReceiptImageException extends DomainException {
    public NonExistentReceiptImageException(String message) {
        super("Non-existent Receipt Image: " + message);
    }
}
