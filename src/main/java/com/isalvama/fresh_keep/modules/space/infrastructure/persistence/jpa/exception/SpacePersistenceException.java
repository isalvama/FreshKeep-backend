package com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.exception;

import com.isalvama.fresh_keep.shared.infrastructure.exception.InfrastructureException;

public class SpacePersistenceException extends InfrastructureException {
    public SpacePersistenceException(String message) {
        super(message);
    }
}
