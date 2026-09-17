package com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class InvalidShoppingReceiptConfirmationException extends DomainException {
    public InvalidShoppingReceiptConfirmationException(String message) {
        super(message);
    }
}
