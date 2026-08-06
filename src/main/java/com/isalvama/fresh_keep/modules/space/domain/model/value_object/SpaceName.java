package com.isalvama.fresh_keep.modules.space.domain.model.value_object;

import com.isalvama.fresh_keep.modules.space.domain.exception.InvalidSpaceNameException;
import com.isalvama.fresh_keep.shared.domain.exception.InvalidIdException;

public record SpaceName(String value) {
    public SpaceName {
        if (value == null || value.isBlank()) {
            throw new InvalidSpaceNameException("value cannot be null or blank");
        }
        if (value.chars().anyMatch(Character::isLetter)){
            throw new InvalidSpaceNameException("value should contain at least 1 letter");
        }
        value = value.trim();
        if (value.length() > 30) {
            throw new InvalidIdException("LocationName cannot have more than 30 characters");
        }
    }

    public static SpaceName from (String value){
        return new SpaceName(value);
    }
}