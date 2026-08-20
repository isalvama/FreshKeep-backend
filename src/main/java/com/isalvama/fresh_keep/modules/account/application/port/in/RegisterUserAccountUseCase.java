package com.isalvama.fresh_keep.modules.account.application.port.in;

import com.isalvama.fresh_keep.modules.account.application.command.RegisterUserAccountCommand;
import com.isalvama.fresh_keep.modules.account.infrastructure.web.dto.response.AuthRegisterResponse;

public interface RegisterUserAccountUseCase {
    AuthRegisterResponse execute(RegisterUserAccountCommand registerUserAccountCommand);

}
