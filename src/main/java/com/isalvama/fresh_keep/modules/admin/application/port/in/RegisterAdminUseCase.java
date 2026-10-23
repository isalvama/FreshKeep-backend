package com.isalvama.fresh_keep.modules.admin.application.port.in;

import com.isalvama.fresh_keep.modules.admin.application.command.RegisterAdminCommand;

public interface RegisterAdminUseCase {
    void execute (RegisterAdminCommand command);
}
