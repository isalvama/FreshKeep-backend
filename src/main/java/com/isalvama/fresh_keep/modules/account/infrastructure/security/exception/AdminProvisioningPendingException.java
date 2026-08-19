package com.isalvama.fresh_keep.modules.account.infrastructure.security.exception;

import com.isalvama.fresh_keep.shared.infrastructure.exception.InfrastructureException;

public class AdminProvisioningPendingException extends InfrastructureException {
    public AdminProvisioningPendingException(String message) {
        super(message);
    }
}
