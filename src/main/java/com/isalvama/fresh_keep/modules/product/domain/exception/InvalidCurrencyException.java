package com.isalvama.fresh_keep.modules.product.domain.exception;

public class InvalidCurrencyException extends InvalidMoneyException {
    public InvalidCurrencyException(String message) {
        super("Invalid Currency: " + message);
    }
}
