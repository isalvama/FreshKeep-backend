package com.isalvama.fresh_keep.modules.space.domain.model.value_object;

import com.isalvama.fresh_keep.shared.domain.exception.InvalidIdException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SpaceIdTest {

    @Test
    void create_generatesNonNullRandomUuid() {
        SpaceId first = SpaceId.create();
        SpaceId second = SpaceId.create();

        assertNotNull(first.value());
        assertNotEquals(first, second);
    }

    @Test
    void of_uuid_returnsSpaceIdWithSameValue() {
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid, SpaceId.of(uuid).value());
    }

    @Test
    void of_uuid_throwsInvalidIdExceptionWhenValueIsNull() {
        Exception exception = assertThrows(InvalidIdException.class, () -> SpaceId.of((UUID) null));
        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    void from_string_returnsSpaceIdWithParsedValue() {
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid, SpaceId.from(uuid.toString()).value());
    }

    @Test
    void from_string_throwsInvalidIdExceptionWhenValueIsNotValidUuid() {
        Exception exception = assertThrows(InvalidIdException.class, () -> SpaceId.from("not-a-uuid"));
        assertTrue(exception.getMessage().contains("invalid UUID format"));
    }

    @Test
    void from_string_throwsInvalidIdExceptionWhenValueIsNull() {
        Exception exception = assertThrows(InvalidIdException.class, () -> SpaceId.from((String) null));
        assertTrue(exception.getMessage().contains("string value"));
        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    void from_string_throwsInvalidIdExceptionWhenValueIsBlank() {
        Exception exception = assertThrows(InvalidIdException.class, () -> SpaceId.from(" "));
        assertTrue(exception.getMessage().contains("blank"));
    }

    @Test
    void from_string_throwsInvalidIdExceptionWhenValueIsEmpty() {
        Exception exception = assertThrows(InvalidIdException.class, () -> SpaceId.from(""));
        assertTrue(exception.getMessage().contains("empty"));
    }

    @Test
    void toString_returnsUuidStringRepresentation() {
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid.toString(), SpaceId.of(uuid).toString());
    }
}
