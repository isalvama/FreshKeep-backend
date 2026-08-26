package com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object;

import com.isalvama.fresh_keep.shared.domain.exception.InvalidIdException;

import java.util.UUID;

public record ReceiptImageId(UUID value) {

    public ReceiptImageId {
        if (value == null) {
            throw new InvalidIdException("value of ReceiptImageId cannot be null.");
        }
    }

    public static ReceiptImageId of(UUID value) {
        return new ReceiptImageId(value);
    }

    public static ReceiptImageId from(String value) {
        if (value == null){
            throw new InvalidIdException("string value to create ReceiptImageId cannot be null.");
        }

        if (value.isBlank()){
            throw new InvalidIdException("string value to create ReceiptImageId cannot be blank or empty.");
        }

        try {
            return new ReceiptImageId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new InvalidIdException("invalid UUID format for ReceiptImageId of '" + value + "'.");
        }
    }

    public static ReceiptImageId create() {
        return new ReceiptImageId(UUID.randomUUID());
    }


    @Override
    public String toString() {
        return value.toString();
    }
}
