package com.isalvama.fresh_keep.modules.account.infrastructure.security;

public record AuthToken(
        String token
) {
    public AuthToken {
        if (token == null || token.isBlank()){
            throw new IllegalArgumentException("Token cannot be null or blank"); //TODO review
        }
    }

    public static AuthToken from (String value){
        return new AuthToken(value);
    }
}
