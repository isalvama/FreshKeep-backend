package com.isalvama.fresh_keep.modules.account.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class ForbiddenException extends DomainException {
    public ForbiddenException(String message) {
        super(message);
    }
}
