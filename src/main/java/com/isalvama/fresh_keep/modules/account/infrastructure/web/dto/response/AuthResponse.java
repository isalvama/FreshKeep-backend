package com.isalvama.fresh_keep.modules.account.infrastructure.web.dto.response;

public record AuthResponse(
        String accountId,
        String email,
        String jwtString
) {
}
