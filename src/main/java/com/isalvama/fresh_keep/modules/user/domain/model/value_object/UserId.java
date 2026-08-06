package com.isalvama.fresh_keep.modules.user.domain.model.value_object;

import com.isalvama.fresh_keep.shared.domain.exception.InvalidIdException;

import java.util.UUID;

public record UserId(UUID value) {

    public UserId {
        if (value == null) {
            throw new InvalidIdException("UserId cannot be null");
        }
    }

    public static UserId of(UUID value) {
        return new UserId(value);
    }

    public static UserId from(String value) {
        try {
            return new UserId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new InvalidIdException("Invalid UUID format for UserId: " + value);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}

