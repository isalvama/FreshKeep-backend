package com.isalvama.fresh_keep.modules.account.infrastructure.exception;

import com.isalvama.fresh_keep.shared.infrastructure.exception.InfrastructureException;

public class AccountPersistenceException extends InfrastructureException {
    public AccountPersistenceException(String message) {
        super("Account Persistence Error: " + message);
    }
}
