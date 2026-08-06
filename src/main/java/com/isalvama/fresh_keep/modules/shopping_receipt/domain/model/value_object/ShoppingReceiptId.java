package com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object;

import com.isalvama.fresh_keep.shared.domain.exception.InvalidIdException;

import java.util.UUID;

public record ShoppingReceiptId(UUID value) {

    public ShoppingReceiptId {
        if (value == null) {
            throw new InvalidIdException("ShoppingId cannot be null");
        }
    }

    public static ShoppingReceiptId of(UUID value) {
        return new ShoppingReceiptId(value);
    }

    public static ShoppingReceiptId of(String value) {
        try {
            return new ShoppingReceiptId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new InvalidIdException("Invalid UUID format for ShoppingId: " + value);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
