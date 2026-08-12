package com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object;

import com.isalvama.fresh_keep.shared.domain.exception.InvalidIdException;

import java.util.UUID;

public record ShoppingReceiptId(UUID value) {

    public ShoppingReceiptId {
        if (value == null) {
            throw new InvalidIdException("value of ShoppingReceiptId cannot be null");
        }
    }

    public static ShoppingReceiptId of(UUID value) {
        return new ShoppingReceiptId(value);
    }

    public static ShoppingReceiptId from(String value) {
        try {
            if (value == null){
                throw new InvalidIdException("string value to create ShoppingReceiptId cannot be null.");
            }

            if (value.isBlank()){
                throw new InvalidIdException("string value to create ShoppingReceiptId cannot be blank or empty.");
            }

            return new ShoppingReceiptId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new InvalidIdException("invalid UUID format for ShoppingReceiptId of '" + value + "'.");
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
