package com.isalvama.fresh_keep.modules.account.infrastructure.security.exception;

import com.isalvama.fresh_keep.shared.infrastructure.exception.InfrastructureException;

public class InvalidResolvedEntitiesException extends InfrastructureException {
    public InvalidResolvedEntitiesException(String message) {
        super("Invalid ResolvedEntities: " + message);
    }
}
