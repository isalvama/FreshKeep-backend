package com.isalvama.fresh_keep.modules.account.infrastructure.security;

import com.isalvama.fresh_keep.modules.account.domain.exception.InvalidTokenException;

public record AuthToken(
        String token
) {
    public AuthToken {
        if (token == null || token.isBlank()){
            throw new InvalidTokenException("The Token cannot be null or blank");
        }
    }

    public static AuthToken from (String value){
        return new AuthToken(value);
    }
}
