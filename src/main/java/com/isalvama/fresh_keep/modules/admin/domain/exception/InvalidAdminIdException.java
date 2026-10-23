package com.isalvama.fresh_keep.modules.admin.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class InvalidAdminIdException extends DomainException {
    public InvalidAdminIdException(String message) {
        super(message);
    }
}
