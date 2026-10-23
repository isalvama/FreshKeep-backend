package com.isalvama.fresh_keep.modules.space.domain.model.value_object;

import com.isalvama.fresh_keep.modules.space.domain.exception.InvalidTokenException;

import java.util.UUID;

public record Token (String value) {
    public Token {
        if (value == null) {
            throw new InvalidTokenException("value of Token cannot be null.");
        }
    }

    public static Token create() {
        return new Token(UUID.randomUUID().toString());
    }


    public static Token from(String value) {
        if (value == null){
            throw new InvalidTokenException("string value to create Token cannot be null.");
        }

        if (value.isBlank()){
            throw new InvalidTokenException("string value to create Token cannot be blank or empty.");
        }

        try {
            return new Token(UUID.fromString(value).toString());
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException("invalid UUID format for Token of '" + value + "'.");
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}

