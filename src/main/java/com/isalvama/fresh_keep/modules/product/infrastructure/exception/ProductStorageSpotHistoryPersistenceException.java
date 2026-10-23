package com.isalvama.fresh_keep.modules.product.infrastructure.exception;

import com.isalvama.fresh_keep.shared.infrastructure.exception.InfrastructureException;

public class ProductStorageSpotHistoryPersistenceException extends InfrastructureException {
    public ProductStorageSpotHistoryPersistenceException(String message) {
        super("Error Persisting Product Storage Spot History: " + message);
    }
}
