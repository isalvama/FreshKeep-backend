package com.isalvama.fresh_keep.modules.account.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class InvalidAccountIdException extends DomainException {
    public InvalidAccountIdException(String message) {
        super("Invalid Account Id: " + message);
    }
}
