package com.isalvama.fresh_keep.modules.product.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class InvalidProductException extends DomainException {
    public InvalidProductException(String message) {
        super("Invalid Product: " + message);
    }
}
