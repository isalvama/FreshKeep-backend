package com.isalvama.fresh_keep.modules.user.infrastructure.exception;

import com.isalvama.fresh_keep.shared.infrastructure.exception.InfrastructureException;

public class UserPersistenceException extends InfrastructureException {
    public UserPersistenceException(String message) {
        super("User Persistence Error: " + message);
    }
}
