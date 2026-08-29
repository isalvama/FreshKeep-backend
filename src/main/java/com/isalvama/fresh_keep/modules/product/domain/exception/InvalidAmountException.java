package com.isalvama.fresh_keep.modules.product.domain.exception;

public class InvalidAmountException extends InvalidMoneyException {
    public InvalidAmountException(String message) {
        super(message);
    }
}
