package com.isalvama.fresh_keep.modules.space.infrastructure.exception;

import com.isalvama.fresh_keep.shared.infrastructure.exception.InfrastructureException;

public class SpacePersistenceException extends InfrastructureException {
    public SpacePersistenceException(String message) {
        super("Error Persisting/Retrieving Space Data: " + message);
    }
}
