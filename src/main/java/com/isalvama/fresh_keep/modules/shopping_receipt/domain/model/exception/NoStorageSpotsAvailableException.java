package com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class NoStorageSpotsAvailableException extends DomainException {
    public NoStorageSpotsAvailableException(String message) {
        super("No Storage Spots Available: " + message);
    }
}