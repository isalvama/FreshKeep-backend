package com.isalvama.fresh_keep.modules.user.domain.model.value_object;

import com.isalvama.fresh_keep.modules.user.domain.exception.InvalidUserNameException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserNameTest {

    @Test
    void of_trimsAndLowercasesValue() {
        assertEquals("john doe", UserName.of("  John Doe  ").value());
    }

    @Test
    void of_throwsInvalidUserNameExceptionWhenValueIsNull() {
        assertThrows(InvalidUserNameException.class, () -> UserName.of(null));
    }

    @Test
    void of_throwsInvalidUserNameExceptionWhenValueIsBlank() {
        assertThrows(InvalidUserNameException.class, () -> UserName.of(" "));
    }

    @Test
    void of_throwsInvalidUserNameExceptionWhenValueIsEmpty() {
        assertThrows(InvalidUserNameException.class, () -> UserName.of(""));
    }

    @Test
    void of_throwsInvalidUserNameExceptionWhenValueIsShorterThanMinLength() {
        assertThrows(InvalidUserNameException.class, () -> UserName.of("a"));
    }

    @Test
    void of_throwsInvalidUserNameExceptionWhenValueExceedsMaxLength() {
        assertThrows(InvalidUserNameException.class, () -> UserName.of("a".repeat(21)));
    }

    @Test
    void of_doesNotThrowInvalidUserNameExceptionWhenValueAfterTrimmedHasValidSize() {
        String name = "a".repeat(20) + " ";
        assertDoesNotThrow(() -> UserName.of(name));
    }

    @Test
    void of_throwsInvalidUserNameExceptionWhenValueContainsDisallowedCharacters() {
        assertThrows(InvalidUserNameException.class, () -> UserName.of("john@doe"));
    }

    @Test
    void of_throwsInvalidUserNameExceptionWhenValueContainsNoLetters() {
        assertThrows(InvalidUserNameException.class, () -> UserName.of("12345"));
    }

    @Test
    void of_allowsInternalSeparators() {
        assertDoesNotThrow(() -> UserName.of("john_doe"));
        assertDoesNotThrow(() -> UserName.of("john.doe"));
        assertDoesNotThrow(() -> UserName.of("john-doe"));
        assertDoesNotThrow(() -> UserName.of("john doe"));
    }

    @Test
    void toString_returnsTrimmedLowercasedValue() {
        assertEquals("john doe", UserName.of("  John Doe  ").toString());
    }
}
