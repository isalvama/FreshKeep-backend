package com.isalvama.fresh_keep.modules.admin.infrastructure.exception;

import com.isalvama.fresh_keep.shared.infrastructure.exception.InfrastructureException;

public class AdminPersistenceException extends InfrastructureException {
    public AdminPersistenceException(String message) {
        super("Admin Persistence Error: " + message);
    }
}
