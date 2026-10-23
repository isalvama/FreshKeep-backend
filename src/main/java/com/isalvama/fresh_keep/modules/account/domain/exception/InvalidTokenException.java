package com.isalvama.fresh_keep.modules.account.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class InvalidTokenException extends DomainException {
    public InvalidTokenException(String message) {
        super("Invalid Token: " + message);
    }
}
