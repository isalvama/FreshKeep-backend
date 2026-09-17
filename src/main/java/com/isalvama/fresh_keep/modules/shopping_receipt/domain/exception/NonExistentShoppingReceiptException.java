package com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class NonExistentShoppingReceiptException extends DomainException {
    public NonExistentShoppingReceiptException(String message) {
        super("Non-existent Receipt Image: " + message);
    }
}
