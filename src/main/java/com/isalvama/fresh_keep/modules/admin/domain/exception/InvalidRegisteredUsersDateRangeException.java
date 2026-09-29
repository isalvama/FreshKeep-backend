package com.isalvama.fresh_keep.modules.admin.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class InvalidRegisteredUsersDateRangeException extends DomainException {
    public InvalidRegisteredUsersDateRangeException(String message) {
        super("Invalid registered users date range: " + message);
    }
}
