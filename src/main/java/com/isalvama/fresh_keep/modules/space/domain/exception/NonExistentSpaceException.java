package com.isalvama.fresh_keep.modules.space.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class NonExistentSpaceException extends DomainException {
    public NonExistentSpaceException(String message) {
        super("Non-existent Space: " + message);
    }
}
