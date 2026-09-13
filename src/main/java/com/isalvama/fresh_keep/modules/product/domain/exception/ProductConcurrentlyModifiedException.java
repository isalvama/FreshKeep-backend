package com.isalvama.fresh_keep.modules.product.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.ConflictException;

public class ProductConcurrentlyModifiedException extends ConflictException {
    public ProductConcurrentlyModifiedException(String message) {
        super(message);
    }
}
