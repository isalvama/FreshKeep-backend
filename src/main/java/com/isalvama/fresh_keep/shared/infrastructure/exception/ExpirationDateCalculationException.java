package com.isalvama.fresh_keep.shared.infrastructure.exception;

public class ExpirationDateCalculationException extends InfrastructureException {
    public ExpirationDateCalculationException(String message) {
        super("Error in expiration date calculation: " + message);
    }
}
