package com.isalvama.fresh_keep.shared.infrastructure.exception;

public class AiRetryableException extends InfrastructureException {
    public AiRetryableException(String message) {
        super(message);
    }

    public AiRetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}
