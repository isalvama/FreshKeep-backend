package com.isalvama.fresh_keep.modules.space.domain.model.value_object;

import com.isalvama.fresh_keep.modules.space.domain.exception.InvalidSpaceNameException;
import java.util.Arrays;
import java.util.stream.Collectors;

public record SpaceName(String value) {

    private static final int MAX_LENGTH = 30;

    public SpaceName {
        if (value == null || value.isBlank()) {
            throw new InvalidSpaceNameException("value cannot be null or blank");
        }
        if (value.chars().noneMatch(Character::isLetter)){
            throw new InvalidSpaceNameException("value should contain at least 1 letter");
        }
        value = value.trim();

        if (value.length() > MAX_LENGTH) {
            throw new InvalidSpaceNameException("value cannot have more than 30 characters");
        }
        value = Arrays.stream(value.split("\\s+"))
                .filter(word -> !word.isEmpty())
                .map(word -> word.substring(0, 1)
                        .toUpperCase() + word.substring(1))
                .collect(Collectors.joining(" "));
    }

    public static SpaceName from (String value){
        return new SpaceName(value);
    }
}