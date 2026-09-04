package com.isalvama.fresh_keep.modules.shopping_receipt.domain.model;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.exception.InvalidReceiptImageException;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.AssetId;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ReceiptImageId;
import lombok.Getter;

@Getter
public class ReceiptImage {
    private final ReceiptImageId id;
    private final AssetId assetId;

    private ReceiptImage(ReceiptImageId id, AssetId assetId) {
        this.id = validateNotNull(id, "id");
        this.assetId = validateNotNull(assetId, "assetId");
    }

    public static ReceiptImage create (AssetId assetId){
        return new ReceiptImage(
                ReceiptImageId.create(),
                assetId
        );
    }

    public static ReceiptImage reconstitute (ReceiptImageId id, AssetId assetId){
        return new ReceiptImage(
                id,
                assetId
        );
    }

    private static <T> T validateNotNull(T fieldValue, String fieldName) {
        if (fieldValue == null)
            throw new InvalidReceiptImageException(fieldName + " of ReceiptImage cannot be null.");
        return fieldValue;
    }


}
