package com.isalvama.fresh_keep.modules.account.infrastructure.security.token;

import com.isalvama.fresh_keep.modules.account.domain.exception.InvalidTokenException;

public record AuthToken(
        String token,
        Long expiration
) {
    public AuthToken {
        if (token == null || token.isBlank()){
            throw new InvalidTokenException("The Token cannot be null or blank");
        }

        if (expiration == null){
            throw new InvalidTokenException("The Token's expiration cannot be null");
        }
    }

    public static AuthToken from (String token, Long expiration){
        return new AuthToken(
                token,
                expiration
        );
    }
}
