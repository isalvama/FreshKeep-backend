package com.isalvama.fresh_keep.modules.account.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthJwtResponse(
        @Schema(description = "Authenticated account's id") String accountId,
        @Schema(description = "Account email") String email,
        @Schema(description = "Bearer JWT; send as 'Authorization: Bearer <jwtString>' on subsequent requests") String jwtString,
        @Schema(description = "Raw jwt.expiration value in milliseconds used to build the token, not a computed remaining time") long expiresIn
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
