package com.isalvama.fresh_keep.modules.shopping_receipt.domain.model;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.exception.InvalidReceiptImageException;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.AssetId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReceiptImageTest {
    private static final AssetId ASSET_ID = AssetId.of("shopping_receipts/receipts/abc123");
    private static final String MIME_TYPE = "image/jpeg";

    @Test
    void create_generatesReceiptImageWithGeneratedId() {
        ReceiptImage receiptImage = ReceiptImage.create(ASSET_ID, MIME_TYPE);

        assertNotNull(receiptImage);
        assertNotNull(receiptImage.getId());
        assertEquals(ASSET_ID, receiptImage.getAssetId());
        assertEquals(MIME_TYPE, receiptImage.getMimeType());
    }

    @Test
    void create_generatesADifferentIdOnEachCall() {
        ReceiptImage first = ReceiptImage.create(ASSET_ID, MIME_TYPE);
        ReceiptImage second = ReceiptImage.create(ASSET_ID, MIME_TYPE);

        assertNotEquals(first.getId(), second.getId());
    }

    @Test
    void create_throwsInvalidReceiptImageExceptionWhenAssetIdIsNull() {
        Exception exception = assertThrows(InvalidReceiptImageException.class, () -> ReceiptImage.create(null, MIME_TYPE));
        assertTrue(exception.getMessage().contains("assetId"));
        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    void create_throwsInvalidReceiptImageExceptionWhenMimeTypeIsBlank() {
        Exception exception = assertThrows(InvalidReceiptImageException.class, () -> ReceiptImage.create(ASSET_ID, " "));
        assertTrue(exception.getMessage().contains("mimeType"));
        assertTrue(exception.getMessage().contains("cannot be null or blank"));
    }
}
