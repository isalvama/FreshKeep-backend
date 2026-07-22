package com.isalvama.fresh_keep.modules.account.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class InvalidEmailException extends DomainException {
    public InvalidEmailException(String message) {
        super("Invalid Email: " + message);
    }
}
