package com.isalvama.fresh_keep.modules.space.domain.model.value_object;

import com.isalvama.fresh_keep.modules.space.domain.exception.InvalidTokenException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TokenTest {

    @Test
    void create_generatesNonBlankUuidToken() {
        Token token = Token.create();

        assertNotNull(token.value());
        assertDoesNotThrow(() -> UUID.fromString(token.value()));
    }

    @Test
    void from_string_returnsTokenWithCanonicalUuidValue() {
        UUID uuid = UUID.randomUUID();

        assertEquals(uuid.toString(), Token.from(uuid.toString()).value());
    }

    @Test
    void from_string_throwsWhenValueIsNullBlankOrInvalid() {
        assertThrows(InvalidTokenException.class, () -> Token.from((String) null));
        assertThrows(InvalidTokenException.class, () -> Token.from(""));
        assertThrows(InvalidTokenException.class, () -> Token.from("not-a-uuid"));
    }

    @Test
    void constructor_throwsWhenValueIsNull() {
        assertThrows(InvalidTokenException.class, () -> new Token(null));
    }

    @Test
    void toString_returnsTokenValue() {
        String value = UUID.randomUUID().toString();

        assertEquals(value, new Token(value).toString());
    }
}
