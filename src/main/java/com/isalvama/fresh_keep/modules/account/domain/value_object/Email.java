package com.isalvama.fresh_keep.modules.account.domain.value_object;

import com.isalvama.fresh_keep.modules.account.domain.exception.InvalidEmailException;

import java.util.regex.Pattern;

public record Email(String value) {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$",
                    Pattern.CASE_INSENSITIVE);

    public Email {
        if (value == null) {
            throw new InvalidEmailException("Email can't be null");
        }

        if (value.isBlank()) {
            throw new InvalidEmailException("Email can't be empty");
        }

        value = value.trim().toLowerCase();

        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new InvalidEmailException("the email's format is invalid");
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