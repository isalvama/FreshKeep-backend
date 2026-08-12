package com.isalvama.fresh_keep.modules.space.domain.model.value_object;

import com.isalvama.fresh_keep.modules.space.domain.exception.InvalidStorageSpotName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StorageSpotNameTest {
    @Test
    void from_returnsWordWithFirstLettersToUppercase(){
        assertEquals("Storage Name", StorageSpotName.from("storage name").value());
    }

    @Test
    void from_throwsInvalidStorageSpotNameExceptionWhenValueExceedsSize30(){
        String nameExceedingSize = "a".repeat(31);
        assertThrows(InvalidStorageSpotName.class, () -> {StorageSpotName.from(nameExceedingSize);});
    }

    @Test
    void from_throwsInvalidStorageSpotNameExceptionWhenValueIsNull(){
        assertThrows(InvalidStorageSpotName.class, () -> {StorageSpotName.from(null);});
    }

    @Test
    void from_throwsInvalidStorageSpotNameExceptionWhenValueIsBlank(){
        String nameExceedingSize = "a".repeat(31);
        assertThrows(InvalidStorageSpotName.class, () -> {StorageSpotName.from(" ");});
    }

    @Test
    void from_doesNotThrowInvalidStorageSpotNameExceptionWhenValueAfterTrimmedHasValidSize(){
        String name = "a".repeat(30) + " ";
        assertDoesNotThrow(() -> {StorageSpotName.from(name);});
    }

    @Test
    void from_throwsInvalidStorageSpotNameExceptionWhenValueIsEmpty(){
        String nameExceedingSize = "a".repeat(31);
        assertThrows(InvalidStorageSpotName.class, () -> {StorageSpotName.from("");});
    }

    @Test
    void from_trimsValue(){
        assertEquals("A A", StorageSpotName.from(" a a ").value());
    }

}