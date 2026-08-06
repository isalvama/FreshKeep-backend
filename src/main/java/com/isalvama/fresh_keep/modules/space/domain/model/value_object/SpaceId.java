package com.isalvama.fresh_keep.modules.space.domain.model.value_object;

import com.isalvama.fresh_keep.shared.domain.exception.InvalidIdException;

import java.util.UUID;

public record SpaceId(UUID value) {

    public SpaceId {
        if (value == null) {
            throw new InvalidIdException("LocationId cannot be null");
        }
    }

    public static SpaceId create() {
        return new SpaceId(UUID.randomUUID());
    }

    public static SpaceId of(UUID value) {
        return new SpaceId(value);
    }

    public static SpaceId of(String value) {
        try {
            return new SpaceId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new InvalidIdException("Invalid UUID format for LocationId: " + value);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}

