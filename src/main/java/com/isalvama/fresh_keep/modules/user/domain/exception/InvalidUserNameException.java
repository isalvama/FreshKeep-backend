package com.isalvama.fresh_keep.modules.user.domain.exception;

public class InvalidUserNameException extends RuntimeException {
    public InvalidUserNameException(String message) {
        super("Invalid UserName: " + message);
    }
}
