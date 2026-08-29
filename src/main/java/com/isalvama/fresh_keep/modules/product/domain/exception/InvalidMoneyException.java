package com.isalvama.fresh_keep.modules.product.domain.exception;

public class InvalidMoneyException extends InvalidProductException {
    public InvalidMoneyException(String message) {
        super("Invalid Money: " + message);
    }
}
