package com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.exception.InvalidAssetIdException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AssetIdTest {

    @Test
    void of_returnsAssetIdWithSameValue() {
        AssetId assetId = AssetId.of("shopping_receipts/receipts/abc123");
        assertEquals("shopping_receipts/receipts/abc123", assetId.value());
    }

    @Test
    void of_trimsSurroundingWhitespace() {
        AssetId assetId = AssetId.of("  abc123  ");
        assertEquals("abc123", assetId.value());
    }

    @Test
    void of_throwsInvalidAssetIdExceptionWhenValueIsNull() {
        Exception exception = assertThrows(InvalidAssetIdException.class, () -> AssetId.of(null));
        assertTrue(exception.getMessage().contains("cannot be null or blank"));
    }

    @Test
    void of_throwsInvalidAssetIdExceptionWhenValueIsEmpty() {
        Exception exception = assertThrows(InvalidAssetIdException.class, () -> AssetId.of(""));
        assertTrue(exception.getMessage().contains("cannot be null or blank"));
    }

    @Test
    void of_throwsInvalidAssetIdExceptionWhenValueIsBlank() {
        Exception exception = assertThrows(InvalidAssetIdException.class, () -> AssetId.of("   "));
        assertTrue(exception.getMessage().contains("cannot be null or blank"));
    }

    @Test
    void reconstitute_returnsAssetIdWithSameValue() {
        AssetId assetId = AssetId.reconstitute("abc123");
        assertEquals("abc123", assetId.value());
    }

    @Test
    void reconstitute_throwsInvalidAssetIdExceptionWhenValueIsNull() {
        assertThrows(InvalidAssetIdException.class, () -> AssetId.reconstitute(null));
    }

    @Test
    void isEmpty_returnsFalseForAnyValidAssetId() {
        // A value that is null, empty or blank is already rejected by the compact constructor,
        // so a successfully constructed AssetId can never report isEmpty() == true.
        assertFalse(AssetId.of("abc123").isEmpty());
    }

    @Test
    void toString_returnsRawValue() {
        assertEquals("abc123", AssetId.of("abc123").toString());
    }

    @Test
    void equals_returnsTrueForSameValue() {
        assertEquals(AssetId.of("abc123"), AssetId.of("abc123"));
    }

    @Test
    void equals_returnsFalseForDifferentValue() {
        assertNotEquals(AssetId.of("abc123"), AssetId.of("xyz789"));
    }
}
