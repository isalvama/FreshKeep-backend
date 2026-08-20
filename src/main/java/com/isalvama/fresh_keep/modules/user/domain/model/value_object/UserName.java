package com.isalvama.fresh_keep.modules.user.domain.model.value_object;

import com.isalvama.fresh_keep.modules.user.domain.exception.InvalidUserNameException;

import java.util.regex.Pattern;

public record UserName (String value) {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 20;
    private static final Pattern USER_NAME_PATTERN =
            Pattern.compile("^[A-Z0-9]+(?:[ _.-][A-Z0-9]+)*$",
                    Pattern.CASE_INSENSITIVE);

    public UserName {
        if (value == null) {
            throw new InvalidUserNameException("UserName cannot be null");
        }

        if (value.isBlank()) {
            throw new InvalidUserNameException("UserName cannot be empty");
        }

        value = value.trim().toLowerCase();

        if (value.length() < MIN_LENGTH) {
            throw new InvalidUserNameException("UserName must be at least 2 characters long.");
        }

        if (value.length() > MAX_LENGTH) {
            throw new InvalidUserNameException("UserName must not exceed 20 characters.");
        }

        if (!USER_NAME_PATTERN.matcher(value).matches()) {
            throw new InvalidUserNameException("UserName has an invalid format - can only contain ASCII letters and numbers with hyphens (-), underscores (_), points (.) and spaces as internal separators");
        }

        if (value.chars().noneMatch(Character::isLetter)){
            throw new InvalidUserNameException("value should contain at least 1 letter");
        }
    }

    public static UserName of(String value) {
        return new UserName(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
