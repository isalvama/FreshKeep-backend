package com.isalvama.fresh_keep.modules.account.infrastructure.web.dto.response;

public record AuthResponse(
        String accountId,
        String email,
        String jwtString,
        long expiresIn
) {
    public static AuthResponse constitute (String accountId, String email, String jwtString, Long expiresIn){
        return new AuthResponse(
                accountId,
                email,
                jwtString,
                expiresIn
        );
    }
}
