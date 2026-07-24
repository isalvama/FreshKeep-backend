package com.isalvama.fresh_keep.modules.account.application.port.in;

import com.isalvama.fresh_keep.modules.account.application.command.LoginCommand;
import com.isalvama.fresh_keep.modules.account.infrastructure.web.dto.response.AuthResponse;

public interface LoginUseCase {
    AuthResponse execute (LoginCommand loginCommand);
}
