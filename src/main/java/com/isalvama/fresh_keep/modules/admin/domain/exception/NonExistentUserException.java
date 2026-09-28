package com.isalvama.fresh_keep.modules.admin.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class NonExistentUserException extends DomainException {
    public NonExistentUserException(String message) {
        super(message);
    }
}
