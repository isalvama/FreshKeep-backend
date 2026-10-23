package com.isalvama.fresh_keep.modules.account.domain.value_object;

import com.isalvama.fresh_keep.modules.account.domain.exception.InvalidAccountIdException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AccountIdTest {

    @Test
    void create_generatesNonNullRandomUuid() {
        AccountId first = AccountId.create();
        AccountId second = AccountId.create();

        assertNotNull(first.value());
        assertNotEquals(first, second);
    }

    @Test
    void of_returnsAccountIdWithSameValue() {
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid, AccountId.of(uuid).value());
    }

    @Test
    void of_throwsIllegalArgumentExceptionWhenValueIsNull() {
        assertThrows(InvalidAccountIdException.class, () -> AccountId.of(null));
    }

    @Test
    void from_returnsAccountIdWithParsedValue() {
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid, AccountId.from(uuid.toString()).value());
    }

    @Test
    void from_throwsInvalidAccountIdExceptionWhenValueIsNotValidUuid() {
        assertThrows(InvalidAccountIdException.class, () -> AccountId.from("not-a-uuid"));
    }

    @Test
    void from_throwsNullPointerExceptionWhenValueIsNull() {
        assertThrows(InvalidAccountIdException.class, () -> AccountId.from(null));
    }

    @Test
    void toString_returnsUuidStringRepresentation() {
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid.toString(), AccountId.of(uuid).toString());
    }
}
