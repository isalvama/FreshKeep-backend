package com.isalvama.fresh_keep.modules.shopping_receipt.domain.model;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.exception.InvalidReceiptImageException;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.AssetId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReceiptImageTest {
    private static final AssetId ASSET_ID = AssetId.of("shopping_receipts/receipts/abc123");

    @Test
    void create_generatesReceiptImageWithGeneratedId() {
        ReceiptImage receiptImage = ReceiptImage.create(ASSET_ID);

        assertNotNull(receiptImage);
        assertNotNull(receiptImage.getId());
        assertEquals(ASSET_ID, receiptImage.getAssetId());
    }

    @Test
    void create_generatesADifferentIdOnEachCall() {
        ReceiptImage first = ReceiptImage.create(ASSET_ID);
        ReceiptImage second = ReceiptImage.create(ASSET_ID);

        assertNotEquals(first.getId(), second.getId());
    }

    @Test
    void create_throwsInvalidReceiptImageExceptionWhenAssetIdIsNull() {
        Exception exception = assertThrows(InvalidReceiptImageException.class, () -> ReceiptImage.create(null));
        assertTrue(exception.getMessage().contains("assetId"));
        assertTrue(exception.getMessage().contains("cannot be null"));
    }
}
