package com.isalvama.fresh_keep.modules.space.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class InvalidSpaceInvitationException extends DomainException {
    public InvalidSpaceInvitationException(String message) {
        super("Invalid Space Invitation: " + message);
    }
}
