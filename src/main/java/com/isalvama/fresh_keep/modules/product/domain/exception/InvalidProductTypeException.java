package com.isalvama.fresh_keep.modules.product.domain.exception;

public class InvalidProductTypeException extends InvalidProductException {
    public InvalidProductTypeException(String message) {
        super("Invalid Product Type: " + message);
    }
}
