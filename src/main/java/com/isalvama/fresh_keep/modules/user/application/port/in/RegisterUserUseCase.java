package com.isalvama.fresh_keep.modules.user.application.port.in;


import com.isalvama.fresh_keep.modules.user.application.command.RegisterUserCommand;

public interface RegisterUserUseCase {
    void execute(RegisterUserCommand registerUserCommand);
}
