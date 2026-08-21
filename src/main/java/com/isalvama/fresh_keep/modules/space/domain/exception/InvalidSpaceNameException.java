package com.isalvama.fresh_keep.modules.space.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class InvalidSpaceNameException extends DomainException {
    public InvalidSpaceNameException(String message) {
        super("Invalid Space Name: " + message);
    }
}
