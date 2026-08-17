package com.isalvama.fresh_keep.shared.domain.value_object;

import com.isalvama.fresh_keep.shared.domain.exception.InvalidEmailException;

import java.util.regex.Pattern;

public record Email(String value) {
    private static final int MIN_LENGTH = 8;

    private static final int MAX_LENGTH = 30;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$",
                    Pattern.CASE_INSENSITIVE);

    public Email {
        if (value == null) {
            throw new InvalidEmailException("Email cannot be null");
        }

        if (value.isBlank()) {
            throw new InvalidEmailException("Email cannot be empty");
        }

        value = value.trim().toLowerCase();

        if (value.length() < MIN_LENGTH) {
            throw new InvalidEmailException("Email must be at least 8 characters long.");
        }

        if (value.length() > MAX_LENGTH) {
            throw new InvalidEmailException("Email must not exceed 30 characters.");
        }

        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new InvalidEmailException("Email has an invalid format");
        }
    }

    public static Email of(String value) {
        return new Email(value);
    }

    @Override
    public String toString() {
        return value;
    }
}