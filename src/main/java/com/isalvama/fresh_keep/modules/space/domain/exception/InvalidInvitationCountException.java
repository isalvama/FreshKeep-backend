package com.isalvama.fresh_keep.modules.space.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class InvalidInvitationCountException extends DomainException {
    public InvalidInvitationCountException(String message) {
        super("Invalid Invitation Count: " + message);
    }
}
