package com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object;

import com.isalvama.fresh_keep.modules.product.domain.model.value_object.ProductId;
import com.isalvama.fresh_keep.shared.domain.exception.InvalidIdException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ShoppingReceiptIdTest {

    @Test
    void of_uuid_returnsShoppingReceiptIdWithSameValue() {
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid, ShoppingReceiptId.of(uuid).value());
    }

    @Test
    void of_uuid_throwsInvalidIdExceptionWhenValueIsNull() {
        Exception exception = assertThrows(InvalidIdException.class, () -> ShoppingReceiptId.of((UUID) null));
        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    void from_string_returnsShoppingReceiptIdWithParsedValue() {
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid, ShoppingReceiptId.from(uuid.toString()).value());
    }

    @Test
    void from_string_throwsInvalidIdExceptionWhenValueIsNotValidUuid() {
        Exception exception = assertThrows(InvalidIdException.class, () -> ShoppingReceiptId.from("not-a-uuid"));
        assertTrue(exception.getMessage().contains("invalid UUID format"));
    }

    @Test
    void from_string_throwsInvalidIdExceptionWhenValueIsNull() {
        Exception exception = assertThrows(InvalidIdException.class, () -> ShoppingReceiptId.from((String) null));
        assertTrue(exception.getMessage().contains("string value"));
        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    void from_string_throwsInvalidIdExceptionWhenValueIsBlank() {
        Exception exception = assertThrows(InvalidIdException.class, () -> ShoppingReceiptId.from(" "));
        assertTrue(exception.getMessage().contains("blank"));
    }

    @Test
    void from_string_throwsInvalidIdExceptionWhenValueIsEmpty() {
        Exception exception = assertThrows(InvalidIdException.class, () -> ShoppingReceiptId.from(""));
        assertTrue(exception.getMessage().contains("empty"));
    }

    @Test
    void toString_returnsUuidStringRepresentation() {
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid.toString(), ShoppingReceiptId.of(uuid).toString());
    }
}
