package com.isalvama.fresh_keep.modules.account.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class UnauthorizedException extends DomainException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
