package com.isalvama.fresh_keep.modules.shopping_receipt.domain.model;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ReceiptImageId;

public class ReceiptImage {
    private final ReceiptImageId id;
    private final String imageUrl;

    private ReceiptImage(ReceiptImageId id, String imageUrl) {
        this.id = id;
        this.imageUrl = imageUrl;
    }

    public ReceiptImage create (String imageUrl){
        return new ReceiptImage(
                ReceiptImageId.create(),
                imageUrl
        );
    }

}
