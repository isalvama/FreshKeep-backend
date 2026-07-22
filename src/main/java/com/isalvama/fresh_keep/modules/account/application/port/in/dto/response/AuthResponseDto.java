package com.isalvama.fresh_keep.modules.account.application.port.in.dto.response;

public record AuthResponseDto(
        String accountId,
        String email,
        String jwtString
) {
}
