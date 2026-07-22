package com.isalvama.fresh_keep.modules.account.application.port.in;

import com.isalvama.fresh_keep.modules.account.application.command.RegisterUserAccountCommand;
import com.isalvama.fresh_keep.modules.account.infrastructure.web.dto.response.AuthResponse;

public interface RegisterUserAccountUseCase {
    AuthResponse execute(RegisterUserAccountCommand registerUserAccountCommand);

}
