package com.isalvama.fresh_keep.modules.account.infrastructure.web.dto.response;

public record AuthRegisterResponse(
        String accountId,
        String email
) {
    public static AuthRegisterResponse constitute (String accountId, String email){
        return new AuthRegisterResponse(
                accountId,
                email
        );
    }
}