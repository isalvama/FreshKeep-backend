package com.isalvama.fresh_keep.shared.infrastructure.exception;

public class UnparseableAiResponseException extends AiRetryableException {
    public UnparseableAiResponseException(String message) {
        super(message);
    }

    public UnparseableAiResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
