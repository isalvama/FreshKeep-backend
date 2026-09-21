package com.isalvama.fresh_keep.modules.space.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class NonExistentSpaceInvitationException extends DomainException {
    public NonExistentSpaceInvitationException(String message) {
        super("Non Existent Space Invitation Error: " + message);
    }
}
