package com.isalvama.fresh_keep.modules.product.application.exception;

import com.isalvama.fresh_keep.shared.application.exception.ApplicationException;

public class MoveProductDataUnavailableException extends ApplicationException {
    public MoveProductDataUnavailableException(String message) {
        super("Unavailable Data To Move Product: " + message);
    }
}
