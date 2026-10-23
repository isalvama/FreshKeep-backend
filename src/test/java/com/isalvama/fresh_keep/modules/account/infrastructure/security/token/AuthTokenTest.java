package com.isalvama.fresh_keep.modules.account.infrastructure.security.token;

import com.isalvama.fresh_keep.modules.account.domain.exception.InvalidTokenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class AuthTokenTest {

    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30";
    private static final Long VALID_EXPIRATION = 3600L;

    @Test
    @DisplayName("Should create AuthToken when data is valid")
    void create_Success() {
        AuthToken authToken = new AuthToken(VALID_TOKEN, VALID_EXPIRATION);

        assertNotNull(authToken);
        assertEquals(VALID_TOKEN, authToken.token());
        assertEquals(VALID_EXPIRATION, authToken.expiration());
    }

    @Test
    @DisplayName("Should create AuthToken using static factory method")
    void from_Success() {
        AuthToken authToken = AuthToken.from(VALID_TOKEN, VALID_EXPIRATION);

        assertNotNull(authToken);
        assertEquals(VALID_TOKEN, authToken.token());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Should throw exception when token is blank or empty")
    void create_FailBlankToken(String invalidToken) {
        assertThrows(InvalidTokenException.class, () -> new AuthToken(invalidToken, VALID_EXPIRATION));
    }

    @Test
    @DisplayName("Should throw exception when token is null")
    void create_FailNullToken() {
        assertThrows(InvalidTokenException.class, () -> new AuthToken(null, VALID_EXPIRATION));
    }

    @Test
    @DisplayName("Should throw exception when expiration is null")
    void create_FailNullExpiration() {
        assertThrows(InvalidTokenException.class, () -> new AuthToken(VALID_TOKEN, null));
    }

    @Test
    @DisplayName("Should throw exception when expiration is negative")
    void create_FailNegativeExpiration() {
        assertThrows(InvalidTokenException.class, () -> new AuthToken(VALID_TOKEN, null));
    }
}