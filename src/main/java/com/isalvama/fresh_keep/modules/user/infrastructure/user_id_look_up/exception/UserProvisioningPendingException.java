package com.isalvama.fresh_keep.modules.user.infrastructure.user_id_look_up.exception;

import com.isalvama.fresh_keep.shared.infrastructure.exception.InfrastructureException;

public class UserProvisioningPendingException extends InfrastructureException {
    public UserProvisioningPendingException(String message) {
        super(message);
    }
}
