package com.isalvama.fresh_keep.modules.account.application.port.in.dto.response;

public record AuthJwtResult(
        String accountId,
        String email,
        String jwtString,
        long expiresIn
) {
    public static AuthJwtResult constitute(String accountId, String email, String jwtString, Long expiresIn){
        return new AuthJwtResult(
                accountId,
                email,
                jwtString,
                expiresIn
        );
    }
}
