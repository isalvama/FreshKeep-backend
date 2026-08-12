package com.isalvama.fresh_keep.modules.space.domain.exception;

public class InvalidSpaceNameException extends RuntimeException {
    public InvalidSpaceNameException(String message) {
        super("Invalid Space Name: " + message);
    }
}
