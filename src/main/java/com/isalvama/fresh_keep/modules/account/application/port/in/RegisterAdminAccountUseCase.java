package com.isalvama.fresh_keep.modules.account.application.port.in;

import com.isalvama.fresh_keep.modules.account.application.command.RegisterAdminAccountCommand;
import com.isalvama.fresh_keep.modules.account.infrastructure.web.dto.response.AuthResponse;

public interface RegisterAdminAccountUseCase {
    AuthResponse execute(RegisterAdminAccountCommand registerAdminAccountCommand);

}
