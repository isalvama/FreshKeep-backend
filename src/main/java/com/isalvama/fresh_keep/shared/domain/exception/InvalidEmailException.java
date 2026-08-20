package com.isalvama.fresh_keep.shared.domain.exception;

public class InvalidEmailException extends DomainException {
    public InvalidEmailException(String message) {
        super("Invalid Email: " + message);
    }
}
