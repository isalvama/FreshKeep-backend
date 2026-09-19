package com.isalvama.fresh_keep.modules.space.domain.model.value_object;

import com.isalvama.fresh_keep.modules.space.domain.exception.InvalidSpaceInvitationIdException;

import java.util.UUID;

public record SpaceInvitationId(UUID value) {
    public SpaceInvitationId {
        if (value == null) {
            throw new InvalidSpaceInvitationIdException("value of SpaceInvitationId cannot be null.");
        }
    }

    public static SpaceInvitationId create() {
        return new SpaceInvitationId(UUID.randomUUID());
    }

    public static SpaceInvitationId of(UUID value) {
        return new SpaceInvitationId(value);
    }

    public static SpaceInvitationId from(String value) {
        if (value == null){
            throw new InvalidSpaceInvitationIdException("string value to create SpaceInvitationId cannot be null.");
        }

        if (value.isBlank()){
            throw new InvalidSpaceInvitationIdException("string value to create SpaceInvitationId cannot be blank or empty.");
        }

        try {
            return new SpaceInvitationId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new InvalidSpaceInvitationIdException("invalid UUID format of " + value + " for SpaceInvitationId.");
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
