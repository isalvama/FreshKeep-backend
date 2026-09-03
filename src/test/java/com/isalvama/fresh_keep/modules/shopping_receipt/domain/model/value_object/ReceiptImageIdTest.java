package com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object;

import com.isalvama.fresh_keep.shared.domain.exception.InvalidIdException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReceiptImageIdTest {

    @Test
    void create_generatesNonNullRandomUuid() {
        ReceiptImageId first = ReceiptImageId.create();
        ReceiptImageId second = ReceiptImageId.create();

        assertNotNull(first.value());
        assertNotEquals(first, second);
    }

    @Test
    void of_uuid_returnsReceiptImageIdWithSameValue() {
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid, ReceiptImageId.of(uuid).value());
    }

    @Test
    void of_uuid_throwsInvalidIdExceptionWhenValueIsNull() {
        Exception exception = assertThrows(InvalidIdException.class, () -> ReceiptImageId.of(null));
        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    void from_string_returnsReceiptImageIdWithParsedValue() {
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid, ReceiptImageId.from(uuid.toString()).value());
    }

    @Test
    void from_string_throwsInvalidIdExceptionWhenValueIsNotValidUuid() {
        Exception exception = assertThrows(InvalidIdException.class, () -> ReceiptImageId.from("not-a-uuid"));
        assertTrue(exception.getMessage().contains("invalid UUID format"));
    }

    @Test
    void from_string_throwsInvalidIdExceptionWhenValueIsNull() {
        Exception exception = assertThrows(InvalidIdException.class, () -> ReceiptImageId.from(null));
        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    void from_string_throwsInvalidIdExceptionWhenValueIsBlank() {
        Exception exception = assertThrows(InvalidIdException.class, () -> ReceiptImageId.from(" "));
        assertTrue(exception.getMessage().contains("blank or empty"));
    }

    @Test
    void from_string_throwsInvalidIdExceptionWhenValueIsEmpty() {
        Exception exception = assertThrows(InvalidIdException.class, () -> ReceiptImageId.from(""));
        assertTrue(exception.getMessage().contains("blank or empty"));
    }

    @Test
    void toString_returnsUuidStringRepresentation() {
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid.toString(), ReceiptImageId.of(uuid).toString());
    }
}
