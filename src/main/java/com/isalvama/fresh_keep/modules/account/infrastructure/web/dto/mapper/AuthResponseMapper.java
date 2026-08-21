package com.isalvama.fresh_keep.modules.account.infrastructure.web.dto.mapper;

import com.isalvama.fresh_keep.modules.account.application.port.in.dto.response.AuthJwtResult;
import com.isalvama.fresh_keep.modules.account.application.port.in.dto.response.AuthRegisterResult;
import com.isalvama.fresh_keep.modules.account.infrastructure.web.dto.response.AuthJwtResponse;
import com.isalvama.fresh_keep.modules.account.infrastructure.web.dto.response.AuthRegisterResponse;

public class AuthResponseMapper {
    public static AuthRegisterResponse toResponse(AuthRegisterResult result) {
        return new AuthRegisterResponse(
                result.accountId(),
                result.email()
        );
    }

    public static AuthJwtResponse toResponse(AuthJwtResult result) {
        return new AuthJwtResponse(
                result.accountId(),
                result.email(),
                result.jwtString(),
                result.expiresIn()
        );
    }
}
