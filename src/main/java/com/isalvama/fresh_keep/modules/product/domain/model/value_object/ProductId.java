package com.isalvama.fresh_keep.modules.product.domain.model.value_object;

import com.isalvama.fresh_keep.shared.domain.exception.InvalidIdException;

import java.util.UUID;

public record ProductId(UUID value) {

    public ProductId {
        if (value == null) {
            throw new InvalidIdException("value of ProductId cannot be null.");
        }
    }

    public static ProductId of(UUID value) {
        return new ProductId(value);
    }

    public static ProductId from(String value) {
        if (value == null){
            throw new InvalidIdException("string value to create ProductId cannot be null.");
        }

        if (value.isBlank()){
            throw new InvalidIdException("string value to create ProductId cannot be blank or empty.");
        }

        try {
            return new ProductId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new InvalidIdException("invalid UUID format for ProductId of '" + value + "'.");
        }
    }

    public static ProductId create(){
        return new ProductId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
