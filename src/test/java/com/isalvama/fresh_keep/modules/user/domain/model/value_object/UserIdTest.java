package com.isalvama.fresh_keep.modules.user.domain.model.value_object;

import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotId;
import com.isalvama.fresh_keep.shared.domain.exception.InvalidIdException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserIdTest {

    @Test
    void create_generatesNonNullRandomUuid() {
        UserId first = UserId.create();
        UserId second = UserId.create();

        assertNotNull(first.value());
        assertNotEquals(first, second);
    }

    @Test
    void of_returnsUserIdWithSameValue() {
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid, UserId.of(uuid).value());
    }

    @Test
    void of_throwsInvalidIdExceptionWhenValueIsNull() {
        Exception exception = assertThrows(InvalidIdException.class, () -> UserId.of(null));
        assertTrue(exception.getMessage().contains("cannot be null"));
    }


    @Test
    void from_returnsUserIdWithParsedValue() {
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid, UserId.from(uuid.toString()).value());
    }

    @Test
    void from_throwsInvalidIdExceptionWhenValueIsNotValidUuid() {
        Exception exception = assertThrows(InvalidIdException.class, () -> UserId.from("not-a-uuid"));
        assertTrue(exception.getMessage().contains("invalid UUID format"));
    }

    @Test
    void from_throwsInvalidIdExceptionWhenValueIsNull() {
        Exception exception = assertThrows(InvalidIdException.class, () -> UserId.from(null));
        assertTrue(exception.getMessage().contains("string value"));
        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    void from_string_throwsInvalidIdExceptionWhenValueIsBlank() {
        Exception exception = assertThrows(InvalidIdException.class, () -> StorageSpotId.from(" "));
        assertTrue(exception.getMessage().contains("blank"));
    }

    @Test
    void from_string_throwsInvalidIdExceptionWhenValueIsEmpty() {
        Exception exception = assertThrows(InvalidIdException.class, () -> StorageSpotId.from(""));
        assertTrue(exception.getMessage().contains("empty"));
    }

    @Test
    void toString_returnsUuidStringRepresentation() {
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid.toString(), UserId.of(uuid).toString());
    }
}
