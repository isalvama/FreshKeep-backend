package com.isalvama.fresh_keep.modules.account.domain.exception;

public class InvalidCredentialsException extends UnauthorizedException {
    public InvalidCredentialsException(String message) {
        super("Invalid Credentials Error: " + message);
    }
}
