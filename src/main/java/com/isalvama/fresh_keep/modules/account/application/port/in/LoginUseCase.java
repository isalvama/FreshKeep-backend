package com.isalvama.fresh_keep.modules.account.application.port.in;

import com.isalvama.fresh_keep.modules.account.application.command.LoginCommand;
import com.isalvama.fresh_keep.modules.account.application.port.in.dto.response.AuthJwtResult;

public interface LoginUseCase {
    AuthJwtResult execute (LoginCommand loginCommand);
}
