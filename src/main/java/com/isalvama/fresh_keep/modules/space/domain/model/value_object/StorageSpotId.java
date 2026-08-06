package com.isalvama.fresh_keep.modules.space.domain.model.value_object;

import com.isalvama.fresh_keep.shared.domain.exception.InvalidIdException;

import java.util.UUID;

public record StorageSpotId(UUID value) {


    public StorageSpotId {
        if (value == null) {
            throw new InvalidIdException("LocationId cannot be null");
        }
    }

    public static StorageSpotId create() {
        return new StorageSpotId(UUID.randomUUID());
    }

    public static StorageSpotId of(UUID value) {
        return new StorageSpotId(value);
    }

    public static StorageSpotId of(String value) {
        try {
            return new StorageSpotId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new InvalidIdException("Invalid UUID format for LocationId: " + value);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
