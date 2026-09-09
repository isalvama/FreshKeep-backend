package com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class InvalidReceiptImageException extends DomainException {
    public InvalidReceiptImageException(String message) {
        super("Invalid Receipt Image: " + message);
    }
}
