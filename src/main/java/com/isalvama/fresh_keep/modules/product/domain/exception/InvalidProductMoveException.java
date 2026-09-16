package com.isalvama.fresh_keep.modules.product.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class InvalidProductMoveException extends DomainException {
    public InvalidProductMoveException(String message) {
        super("Invalid Product Move: " + message);
    }
}
