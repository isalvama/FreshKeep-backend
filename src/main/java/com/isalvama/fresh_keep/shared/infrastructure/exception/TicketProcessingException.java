package com.isalvama.fresh_keep.shared.infrastructure.exception;

public class TicketProcessingException extends InfrastructureException {
    public TicketProcessingException(String message) {
        super(message);
    }

    public TicketProcessingException(String message, Throwable cause) {
        super("Internal AI error while processing the receipt: " + message, cause);
    }
}
