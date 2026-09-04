package com.isalvama.fresh_keep.modules.space.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.ConflictException;

public class SpaceNotAvailableForParticipantException extends ConflictException {
    public SpaceNotAvailableForParticipantException(String message) {
        super("Space Not Available For User :" + message);
    }
}
