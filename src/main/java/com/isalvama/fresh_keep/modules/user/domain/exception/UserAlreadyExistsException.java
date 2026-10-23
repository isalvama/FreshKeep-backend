package com.isalvama.fresh_keep.modules.user.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.ConflictException;

public class UserAlreadyExistsException extends ConflictException {
    public UserAlreadyExistsException(String message) {
        super("User Already Exists: " + message);
    }
}
