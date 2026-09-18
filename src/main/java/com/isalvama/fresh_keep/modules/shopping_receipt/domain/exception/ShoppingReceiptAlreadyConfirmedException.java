package com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.ConflictException;

public class ShoppingReceiptAlreadyConfirmedException extends ConflictException {
    public ShoppingReceiptAlreadyConfirmedException(String message) {
        super(message);
    }
}
