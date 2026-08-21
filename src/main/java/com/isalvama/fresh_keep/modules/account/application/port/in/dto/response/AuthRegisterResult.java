package com.isalvama.fresh_keep.modules.account.application.port.in.dto.response;

public record AuthRegisterResult(
        String accountId,
        String email
) {
    public static AuthRegisterResult constitute(String accountId, String email){
        return new AuthRegisterResult(
                accountId,
                email
        );
    }
}
