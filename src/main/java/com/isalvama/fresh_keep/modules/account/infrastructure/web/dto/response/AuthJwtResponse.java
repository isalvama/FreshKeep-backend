package com.isalvama.fresh_keep.modules.account.infrastructure.web.dto.response;

public record AuthJwtResponse(
        String accountId,
        String email,
        String jwtString,
        long expiresIn
) {
    public static AuthJwtResponse constitute (String accountId, String email, String jwtString, Long expiresIn){
        return new AuthJwtResponse(
                accountId,
                email,
                jwtString,
                expiresIn
        );
    }
}
