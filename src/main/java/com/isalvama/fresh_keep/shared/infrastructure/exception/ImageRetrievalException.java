package com.isalvama.fresh_keep.shared.infrastructure.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class ImageRetrievalException extends DomainException {
    public ImageRetrievalException(String message) {
        super("Error retrieving image: " + message);
    }
}
