package com.isalvama.fresh_keep.modules.product.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class InvalidMoneyException extends DomainException {
    public InvalidMoneyException(String message) {
        super("Invalid Money: " + message);
    }
}
