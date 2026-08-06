package com.isalvama.fresh_keep.modules.product.domain.model.value_object;

import com.isalvama.fresh_keep.shared.domain.exception.InvalidIdException;

import java.util.UUID;

public record ProductId(UUID value) {

    public ProductId {
        if (value == null) {
            throw new InvalidIdException("ProductId cannot be null");
        }
    }

    public static ProductId of(UUID value) {
        return new ProductId(value);
    }

    public static ProductId of(String value) {
        try {
            return new ProductId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new InvalidIdException("Invalid UUID format for ProductId: " + value);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
