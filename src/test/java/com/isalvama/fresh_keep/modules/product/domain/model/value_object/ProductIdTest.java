package com.isalvama.fresh_keep.modules.product.domain.model.value_object;

import com.isalvama.fresh_keep.shared.domain.exception.InvalidIdException;
import jakarta.validation.constraints.AssertTrue;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductIdTest {

    @Test
    void of_uuid_returnsProductIdWithSameValue() {
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid, ProductId.of(uuid).value());
    }

    @Test
    void of_uuid_throwsInvalidIdExceptionWhenValueIsNull() {
        Exception exception = assertThrows(InvalidIdException.class, () -> ProductId.of((UUID) null));
        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    void from_string_returnsProductIdWithParsedValue() {
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid, ProductId.from(uuid.toString()).value());
    }

    @Test
    void from_string_throwsInvalidIdExceptionWhenValueIsNotValidUuid() {
        Exception exception = assertThrows(InvalidIdException.class, () -> ProductId.from("not-a-uuid"));
        assertTrue(exception.getMessage().contains("invalid UUID format"));
    }

    @Test
    void from_string_throwsInvalidIdExceptionWhenValueIsNull() {
        Exception exception = assertThrows(InvalidIdException.class, () -> ProductId.from((String) null));
        assertTrue(exception.getMessage().contains("string value"));
        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    void from_string_throwsInvalidIdExceptionWhenValueIsBlank() {
        Exception exception = assertThrows(InvalidIdException.class, () -> ProductId.from(" "));
        assertTrue(exception.getMessage().contains("blank"));
    }

    @Test
    void from_string_throwsInvalidIdExceptionWhenValueIsEmpty() {
        Exception exception = assertThrows(InvalidIdException.class, () -> ProductId.from(""));
        assertTrue(exception.getMessage().contains("empty"));
    }

    @Test
    void toString_returnsUuidStringRepresentation() {
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid.toString(), ProductId.of(uuid).toString());
    }
}
