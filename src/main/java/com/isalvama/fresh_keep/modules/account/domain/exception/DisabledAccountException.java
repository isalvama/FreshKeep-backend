package com.isalvama.fresh_keep.modules.account.domain.exception;

public class DisabledAccountException extends ForbiddenException {
    public DisabledAccountException(String message) {
        super("Disabled Account Error: " + message);
    }
}
