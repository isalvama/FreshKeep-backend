package com.isalvama.fresh_keep.modules.account.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.ConflictException;

public class AccountAlreadyExistsException extends ConflictException {
    public AccountAlreadyExistsException(String message) {
        super("Account Already Exists: " + message);
    }
}
