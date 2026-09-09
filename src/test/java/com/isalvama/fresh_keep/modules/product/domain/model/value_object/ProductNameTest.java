package com.isalvama.fresh_keep.modules.product.domain.model.value_object;

import com.isalvama.fresh_keep.modules.product.domain.exception.InvalidProductNameException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductNameTest {
    @Test
    void from_returnsFirstLetterOfEachWordUppercased() {
        assertEquals("Tomatoes And Onions", ProductName.from("tomatoes and onions").value());
    }

    @Test
    void from_trimsValue() {
        assertEquals("A A", ProductName.from(" a a ").value());
    }

    @Test
    void from_collapsesMultipleSpacesBetweenWords() {
        assertEquals("Tomato Sauce", ProductName.from("tomato   sauce").value());
    }

    @Test
    void from_throwsInvalidProductNameExceptionWhenValueIsNull() {
        assertThrows(InvalidProductNameException.class, () -> ProductName.from(null));
    }

    @Test
    void from_throwsInvalidProductNameExceptionWhenValueIsBlank() {
        assertThrows(InvalidProductNameException.class, () -> ProductName.from(" "));
    }

    @Test
    void from_throwsInvalidProductNameExceptionWhenValueIsEmpty() {
        assertThrows(InvalidProductNameException.class, () -> ProductName.from(""));
    }

    @Test
    void from_throwsInvalidProductNameExceptionWhenValueHasNoLetters() {
        assertThrows(InvalidProductNameException.class, () -> ProductName.from("12345"));
    }

    @Test
    void from_throwsInvalidProductNameExceptionWhenValueExceedsSize30() {
        String nameExceedingSize = "a".repeat(31);
        assertThrows(InvalidProductNameException.class, () -> ProductName.from(nameExceedingSize));
    }

    @Test
    void from_doesNotThrowInvalidProductNameExceptionWhenValueAfterTrimmedHasValidSize() {
        String name = "a".repeat(30) + " ";
        assertDoesNotThrow(() -> ProductName.from(name));
    }
}
