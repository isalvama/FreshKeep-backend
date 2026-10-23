package com.isalvama.fresh_keep.modules.space.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class ExpiredSpaceInvitationException extends DomainException {
    public ExpiredSpaceInvitationException(String message) {
        super("Space Invitation Expiration Error: " + message);
    }
}
