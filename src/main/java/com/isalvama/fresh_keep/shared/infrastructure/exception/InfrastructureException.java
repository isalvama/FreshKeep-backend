package com.isalvama.fresh_keep.shared.infrastructure.exception;

import com.isalvama.fresh_keep.shared.domain.exception.FreshKeepException;

public class InfrastructureException extends FreshKeepException {
    public InfrastructureException(String message) {
        super(message);
    }

    public InfrastructureException(String message, Throwable cause) {
        super(message, cause);
    }
}
