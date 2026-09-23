package com.isalvama.fresh_keep.modules.admin.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class InvalidPaginationException extends DomainException {
    public InvalidPaginationException(String message) {
        super("Invalid Pagination: " + message);
    }
}
