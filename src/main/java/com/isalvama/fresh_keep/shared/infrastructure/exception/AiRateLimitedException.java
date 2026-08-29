package com.isalvama.fresh_keep.shared.infrastructure.exception;

public class AiRateLimitedException extends InfrastructureException {
    public AiRateLimitedException(String message, Throwable cause) {
        super("AI Rate Limit Exceeded: " + message, cause);
    }
}
