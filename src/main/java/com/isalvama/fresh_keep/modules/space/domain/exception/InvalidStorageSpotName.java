package com.isalvama.fresh_keep.modules.space.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class InvalidStorageSpotName extends DomainException {
    public InvalidStorageSpotName(String message) {
        super("Invalid Storage Spot Name: " + message);
    }
}
