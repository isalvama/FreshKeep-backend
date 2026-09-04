package com.isalvama.fresh_keep.modules.product.domain.exception;

public class InvalidProductNameException extends InvalidProductException {
    public InvalidProductNameException(String message) {
        super("Invalid Product Name: " + message);
    }
}
