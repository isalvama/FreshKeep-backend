package com.isalvama.fresh_keep.shared.domain.value_object;

import com.isalvama.fresh_keep.shared.domain.exception.InvalidEmailException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailTest {

    @Test
    void of_trimsAndLowercasesValue() {
        assertEquals("test@example.com", Email.of("  Test@Example.COM  ").value());
    }

    @Test
    void of_throwsInvalidEmailExceptionWhenValueIsNull() {
        assertThrows(InvalidEmailException.class, () -> Email.of(null));
    }

    @Test
    void of_throwsInvalidEmailExceptionWhenValueIsBlank() {
        assertThrows(InvalidEmailException.class, () -> Email.of(" "));
    }

    @Test
    void of_throwsInvalidEmailExceptionWhenValueIsEmpty() {
        assertThrows(InvalidEmailException.class, () -> Email.of(""));
    }

    @Test
    void of_throwsInvalidEmailExceptionWhenValueIsShorterThanMinLength() {
        assertThrows(InvalidEmailException.class, () -> Email.of("a@b.c"));
    }

    @Test
    void of_throwsInvalidEmailExceptionWhenValueExceedsMaxLength() {
        String localPart = "a".repeat(25);
        String tooLong = localPart + "@b.com";
        assertTrue(tooLong.length() > 30);
        assertThrows(InvalidEmailException.class, () -> Email.of(tooLong));
    }

    @Test
    void of_throwsInvalidEmailExceptionWhenValueHasInvalidFormat() {
        assertThrows(InvalidEmailException.class, () -> Email.of("not-an-email"));
    }

    @Test
    void of_doesNotThrowWhenValueIsValid() {
        assertDoesNotThrow(() -> Email.of("test@example.com"));
    }

    @Test
    void toString_returnsTrimmedLowercasedValue() {
        assertEquals("test@example.com", Email.of("  Test@Example.COM  ").toString());
    }
}
