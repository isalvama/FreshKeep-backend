package com.isalvama.fresh_keep.modules.space.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class InvalidSpaceInvitationIdException extends DomainException {
    public InvalidSpaceInvitationIdException(String message) {
        super("Invalid Space Invitation id: " + message);
    }
}
