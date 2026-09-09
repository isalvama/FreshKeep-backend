package com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.ConflictException;

public class SpaceNotAccessibleException extends ConflictException {
    public SpaceNotAccessibleException(String message) {
        super("Space Not Accessible: " + message);
    }
}
