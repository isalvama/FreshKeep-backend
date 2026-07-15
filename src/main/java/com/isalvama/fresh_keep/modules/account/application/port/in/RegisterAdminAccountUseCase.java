package com.isalvama.fresh_keep.modules.account.application.port.in;

import com.isalvama.fresh_keep.modules.account.application.command.RegisterAdminAccountCommand;
import com.isalvama.fresh_keep.modules.account.application.port.in.dto.response.AuthResponseDto;

public interface RegisterAdminAccountUseCase {
    AuthResponseDto execute(RegisterAdminAccountCommand registerAdminAccountCommand);

}
