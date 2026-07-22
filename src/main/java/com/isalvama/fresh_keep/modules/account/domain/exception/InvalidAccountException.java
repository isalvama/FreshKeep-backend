package com.isalvama.fresh_keep.modules.account.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class InvalidAccountException extends DomainException {
    public InvalidAccountException(String message) {
        super("Account User: " + message);
    }
}
