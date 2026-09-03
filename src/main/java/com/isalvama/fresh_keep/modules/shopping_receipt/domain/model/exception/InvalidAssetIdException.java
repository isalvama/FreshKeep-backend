package com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class InvalidAssetIdException extends DomainException {
    public InvalidAssetIdException(String message) {
        super("Invalid AssetId of Receipt Image: " + message);
    }
}
