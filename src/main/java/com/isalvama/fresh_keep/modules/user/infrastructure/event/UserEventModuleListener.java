package com.isalvama.fresh_keep.modules.user.infrastructure.event;

import com.isalvama.fresh_keep.modules.account.domain.event.UserAccountRegisteredEvent;
import com.isalvama.fresh_keep.modules.user.application.command.RegisterUserCommand;
import com.isalvama.fresh_keep.modules.user.application.port.in.RegisterUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventModuleListener {
    private final RegisterUserUseCase registerUserUseCase;

    @EventListener
    public void handle (UserAccountRegisteredEvent userAccountRegisteredEvent){
        registerUserUseCase.execute(
                RegisterUserCommand.builder()
                .accountId(userAccountRegisteredEvent.accountId().toString())
                .email(userAccountRegisteredEvent.email())
                .build()
        );
    }
}
