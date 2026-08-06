package com.isalvama.fresh_keep.shared.domain.exception;

public class InvalidIdException extends DomainException {
    public InvalidIdException(String message) {
        super("Invalid Id: " + message);
    }
}
