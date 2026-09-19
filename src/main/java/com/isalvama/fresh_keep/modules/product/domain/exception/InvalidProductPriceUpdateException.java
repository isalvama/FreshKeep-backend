package com.isalvama.fresh_keep.modules.product.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class InvalidProductPriceUpdateException extends DomainException {
    public InvalidProductPriceUpdateException(String message) {
        super("Invalid Update of Product's price: " + message);
    }
}
