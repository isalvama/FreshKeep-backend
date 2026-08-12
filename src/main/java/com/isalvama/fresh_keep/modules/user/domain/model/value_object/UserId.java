package com.isalvama.fresh_keep.modules.user.domain.model.value_object;

import com.isalvama.fresh_keep.shared.domain.exception.InvalidIdException;

import java.util.UUID;

public record UserId(UUID value) {

    public UserId {
        if (value == null) {
            throw new InvalidIdException("value of UserId cannot be null.");
        }
    }

    public static UserId create() {
        return new UserId(UUID.randomUUID());
    }

    public static UserId of(UUID value) {
        return new UserId(value);
    }

    public static UserId from(String value) {
        if (value == null){
            throw new InvalidIdException("string value to create UserId cannot be null.");
        }

        if (value.isBlank()){
            throw new InvalidIdException("string value to create UserId cannot be blank or empty.");
        }

        try {
            return new UserId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new InvalidIdException("invalid UUID format for UserId of '" + value + "'.");
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}

