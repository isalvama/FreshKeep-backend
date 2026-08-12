package com.isalvama.fresh_keep.modules.space.domain.model.value_object;

import com.isalvama.fresh_keep.modules.space.domain.exception.InvalidStorageSpotName;

import java.util.Arrays;
import java.util.stream.Collectors;

public record StorageSpotName(String value) {
    public StorageSpotName {
        if (value == null || value.isBlank()) {
            throw new InvalidStorageSpotName("value cannot be null or blank");
        }
        if (!value.chars().anyMatch(Character::isLetter)){
            throw new InvalidStorageSpotName("value should contain at least 1 letter");
        }
        value = value.trim();
        if (value.length() > 30) {
            throw new InvalidStorageSpotName("value cannot have more than 30 characters");
        }
        value = Arrays.stream(value.split("\\s+"))
                .filter(word -> !word.isEmpty())
                .map(word -> word.substring(0, 1)
                        .toUpperCase() + word.substring(1))
                .collect(Collectors.joining(" "));
    }

    public static StorageSpotName from (String value){
        return new StorageSpotName(value);
    }
}
