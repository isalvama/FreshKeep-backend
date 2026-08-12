package com.isalvama.fresh_keep.modules.space.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class InvalidSpaceException extends DomainException {
    public InvalidSpaceException(String message) {
        super("Invalid Space: " + message);
    }
}
