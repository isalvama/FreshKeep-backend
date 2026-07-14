package com.isalvama.fresh_keep.modules.account.application.port.in;

import com.isalvama.fresh_keep.modules.account.application.command.RegisterAccountCommand;
import com.isalvama.fresh_keep.modules.account.application.port.in.dto.response.AuthResponseDto;

public interface AuthenticationUseCase {
    AuthResponseDto register (RegisterAccountCommand registerUserAccountCommand);
}
