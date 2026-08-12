package com.isalvama.fresh_keep.modules.space.domain.model.value_object;

import com.isalvama.fresh_keep.shared.domain.exception.InvalidIdException;

import java.util.UUID;

public record SpaceId(UUID value) {

    public SpaceId {
        if (value == null) {
            throw new InvalidIdException("value of SpaceId cannot be null.");
        }
    }

    public static SpaceId create() {
        return new SpaceId(UUID.randomUUID());
    }

    public static SpaceId of(UUID value) {
        return new SpaceId(value);
    }

    public static SpaceId from(String value) {
        if (value == null){
            throw new InvalidIdException("string value to create SpaceId cannot be null.");
        }

        if (value.isBlank()){
            throw new InvalidIdException("string value to create SpaceId cannot be blank or empty.");
        }

        try {
            return new SpaceId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new InvalidIdException("invalid UUID format for SpaceId of '" + value + "'.");
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}

