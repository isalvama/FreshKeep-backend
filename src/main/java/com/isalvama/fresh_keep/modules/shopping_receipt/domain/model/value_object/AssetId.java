package com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.exception.InvalidAssetIdException;

public record AssetId(String value) {
    public AssetId {
        if (value == null || value.isBlank()) {
            throw new InvalidAssetIdException("value cannot be null or blank");
        } else {
            value = value.trim();
        }
    }

    public boolean isEmpty() {
        return value.isEmpty();
    }

    public static AssetId of(String value) {
        return new AssetId(value);
    }

    public static AssetId reconstitute(String value) {
        return new AssetId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
