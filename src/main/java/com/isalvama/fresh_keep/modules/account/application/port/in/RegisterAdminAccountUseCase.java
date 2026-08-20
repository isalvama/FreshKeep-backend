package com.isalvama.fresh_keep.modules.account.application.port.in;

import com.isalvama.fresh_keep.modules.account.application.command.RegisterAdminAccountCommand;
import com.isalvama.fresh_keep.modules.account.infrastructure.web.dto.response.AuthJwtResponse;
import com.isalvama.fresh_keep.modules.account.infrastructure.web.dto.response.AuthRegisterResponse;

public interface RegisterAdminAccountUseCase {
    AuthRegisterResponse execute(RegisterAdminAccountCommand registerAdminAccountCommand);

}
