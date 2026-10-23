package com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class InvalidLanguageException extends DomainException {
    public InvalidLanguageException(String message) {
        super("Invalid Language: " + message);
    }
}
