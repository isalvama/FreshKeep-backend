package com.isalvama.fresh_keep.modules.account.application.port.in;

import com.isalvama.fresh_keep.modules.account.application.command.RegisterUserAccountCommand;
import com.isalvama.fresh_keep.modules.account.application.port.in.dto.response.AuthResponseDto;

public interface RegisterUserAccountUseCase {
    AuthResponseDto execute(RegisterUserAccountCommand registerUserAccountCommand);

}
