package com.isalvama.fresh_keep.shared.infrastructure.exception;

public class AiUnprocessableInputException extends InfrastructureException {
    public AiUnprocessableInputException(String message) {
        super(message);
    }

    public AiUnprocessableInputException(String message, Throwable cause) {
        super("Unprocessable Ticket Data: " + message, cause);
    }
}
