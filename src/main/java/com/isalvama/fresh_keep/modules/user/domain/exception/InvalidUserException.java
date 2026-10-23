package com.isalvama.fresh_keep.modules.user.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class InvalidUserException extends DomainException {
    public InvalidUserException(String message) {
        super("Invalid User: " + message);
    }
}
