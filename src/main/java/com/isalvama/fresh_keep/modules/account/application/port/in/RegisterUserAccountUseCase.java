package com.isalvama.fresh_keep.modules.account.application.port.in;

import com.isalvama.fresh_keep.modules.account.application.command.RegisterUserAccountCommand;
import com.isalvama.fresh_keep.modules.account.application.port.in.dto.response.AuthRegisterResult;

public interface RegisterUserAccountUseCase {
    AuthRegisterResult execute(RegisterUserAccountCommand registerUserAccountCommand);

}
