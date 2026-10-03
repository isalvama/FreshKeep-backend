package com.isalvama.fresh_keep.modules.account.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthRegisterResponse(
        @Schema(description = "Newly created (or promoted) account's id") String accountId,
        @Schema(description = "Account email") String email
) {
    public static AuthRegisterResponse constitute (String accountId, String email){
        return new AuthRegisterResponse(
                accountId,
                email
        );
    }
}