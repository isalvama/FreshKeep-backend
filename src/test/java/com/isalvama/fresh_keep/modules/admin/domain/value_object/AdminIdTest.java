package com.isalvama.fresh_keep.modules.admin.domain.value_object;

import com.isalvama.fresh_keep.modules.admin.domain.exception.InvalidAdminIdException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminIdTest {

    @Test
    void create_shouldGenerateUniqueUuid() {
        AdminId first = AdminId.create();
        AdminId second = AdminId.create();

        assertNotNull(first.value());
        assertNotEquals(first, second);
    }

    @Test
    void of_shouldPreserveUuid() {
        UUID uuid = UUID.randomUUID();

        assertEquals(uuid, AdminId.of(uuid).value());
    }

    @Test
    void of_shouldRejectNullUuid() {
        InvalidAdminIdException exception = assertThrows(
                InvalidAdminIdException.class,
                () -> AdminId.of(null));

        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    void from_shouldParseUuidString() {
        UUID uuid = UUID.randomUUID();

        assertEquals(uuid, AdminId.from(uuid.toString()).value());
    }

    @Test
    void from_shouldRejectInvalidUuidString() {
        InvalidAdminIdException exception = assertThrows(
                InvalidAdminIdException.class,
                () -> AdminId.from("not-a-uuid"));

        assertTrue(exception.getMessage().contains("Invalid AdminId format"));
    }

    @Test
    void from_shouldRejectNullString() {
        InvalidAdminIdException exception = assertThrows(
                InvalidAdminIdException.class,
                () -> AdminId.from(null));

        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    void toString_shouldReturnUuidString() {
        UUID uuid = UUID.randomUUID();

        assertEquals(uuid.toString(), AdminId.of(uuid).toString());
    }
}
