package com.isalvama.fresh_keep.modules.space.domain.model.value_object;

import com.isalvama.fresh_keep.modules.space.domain.exception.InvalidSpaceNameException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpaceNameTest {

    @Test
    void from_returnsWordWithFirstLettersToUppercase(){
        assertEquals("Space Name", SpaceName.from("space name").value());
    }

    @Test
    void from_throwsInvalidSpaceNameExceptionWhenValueExceedsSize30(){
        String nameExceedingSize = "a".repeat(31);
        assertThrows(InvalidSpaceNameException.class, () -> {SpaceName.from(nameExceedingSize);});
    }

    @Test
    void from_throwsInvalidSpaceNameExceptionWhenValueIsNull(){
        assertThrows(InvalidSpaceNameException.class, () -> {SpaceName.from(null);});
    }

    @Test
    void from_throwsInvalidSpaceNameExceptionWhenValueIsBlank(){
        String nameExceedingSize = "a".repeat(31);
        assertThrows(InvalidSpaceNameException.class, () -> {SpaceName.from(" ");});
    }

    @Test
    void from_doesNotThrowInvalidSpaceNameExceptionWhenValueAfterTrimmedHasValidSize(){
        String name = "a".repeat(30) + " ";
        assertDoesNotThrow(() -> {SpaceName.from(name);});
    }

    @Test
    void from_throwsInvalidSpaceNameExceptionWhenValueIsEmpty(){
        String nameExceedingSize = "a".repeat(31);
        assertThrows(InvalidSpaceNameException.class, () -> {SpaceName.from("");});
    }

    @Test
    void from_trimsValue(){
        assertEquals("A A", SpaceName.from(" a a ").value());
    }
}