package com.isalvama.fresh_keep.modules.product.infrastructure.exception;

import com.isalvama.fresh_keep.shared.infrastructure.exception.InfrastructureException;

public class ProductPersistenceException extends InfrastructureException {
    public ProductPersistenceException(String message) {
        super("Error Persisting Product: " + message);
    }
}
