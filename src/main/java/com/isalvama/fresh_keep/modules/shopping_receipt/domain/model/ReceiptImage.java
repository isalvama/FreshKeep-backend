package com.isalvama.fresh_keep.modules.shopping_receipt.domain.model;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.InvalidReceiptImageException;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.AssetId;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ReceiptImageId;
import lombok.Getter;

@Getter
public class ReceiptImage {
    private final ReceiptImageId id;
    private final AssetId assetId;
    private final String mimeType;

    private ReceiptImage(ReceiptImageId id, AssetId assetId, String mimeType) {
        this.id = validateNotNull(id, "id");
        this.assetId = validateNotNull(assetId, "assetId");
        this.mimeType = validateNotBlank(mimeType, "mimeType");
    }

    public static ReceiptImage create (AssetId assetId, String mimeType){
        return new ReceiptImage(
                ReceiptImageId.create(),
                assetId,
                mimeType
        );
    }

    public static ReceiptImage reconstitute (ReceiptImageId id, AssetId assetId, String mimeType){
        return new ReceiptImage(
                id,
                assetId,
                mimeType
        );
    }

    private static <T> T validateNotNull(T fieldValue, String fieldName) {
        if (fieldValue == null)
            throw new InvalidReceiptImageException(fieldName + " of ReceiptImage cannot be null.");
        return fieldValue;
    }

    private static String validateNotBlank(String fieldValue, String fieldName) {
        if (fieldValue == null || fieldValue.isBlank())
            throw new InvalidReceiptImageException(fieldName + " of ReceiptImage cannot be null or blank.");
        return fieldValue;
    }

}
