package com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class InvalidSpaceReferenceException extends DomainException {
    public InvalidSpaceReferenceException(String message) {
        super("Invalid Space Reference: " + message);
    }
}
