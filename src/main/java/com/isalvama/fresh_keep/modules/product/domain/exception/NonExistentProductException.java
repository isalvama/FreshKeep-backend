package com.isalvama.fresh_keep.modules.product.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class NonExistentProductException extends DomainException {
    public NonExistentProductException(String message) {
        super("Non-existent Product: " + message);
    }
}
