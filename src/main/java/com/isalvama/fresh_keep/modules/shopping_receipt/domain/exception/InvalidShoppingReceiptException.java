package com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class InvalidShoppingReceiptException extends DomainException {
    public InvalidShoppingReceiptException(String message) {
        super("Invalid Shopping Receipt: " + message);
    }
}
