package com.isalvama.fresh_keep.modules.admin.domain.value_object;

import com.isalvama.fresh_keep.modules.admin.domain.exception.InvalidAdminIdException;

import java.util.UUID;

public record AdminId (UUID value) {

    public AdminId {
        if (value == null) throw new InvalidAdminIdException("value of AdminId cannot be null");
    }

    public static AdminId create() {
        return new AdminId(UUID.randomUUID());
    }
    public static AdminId of(UUID value) {
        return new AdminId(value);
    }

    public static AdminId from(String value) {
        if (value == null) {
            throw new InvalidAdminIdException("string value to create AdminId cannot be null.");
        }
        try {
            return new AdminId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new InvalidAdminIdException("Invalid AdminId format of String " + value + ". " + e.getMessage());
        }
    }

    @Override
    public String toString() {
            return value.toString();
        }
}
