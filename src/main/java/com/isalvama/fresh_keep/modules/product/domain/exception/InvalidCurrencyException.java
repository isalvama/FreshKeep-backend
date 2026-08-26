package com.isalvama.fresh_keep.modules.product.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class InvalidCurrencyException extends DomainException {
    public InvalidCurrencyException(String message) {
        super("Invalid Currency: " + message);
    }
}
