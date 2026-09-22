package com.isalvama.fresh_keep.modules.admin.infrastructure.event;

import com.isalvama.fresh_keep.modules.account.domain.event.AdminAccountRegisteredEvent;
import com.isalvama.fresh_keep.modules.admin.application.command.RegisterAdminCommand;
import com.isalvama.fresh_keep.modules.admin.application.port.in.RegisterAdminUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
@Component
@RequiredArgsConstructor
public class AdminEventModuleListener {
    private final RegisterAdminUseCase registerAdminUseCase;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle (AdminAccountRegisteredEvent adminAccountRegisteredEvent){
        registerAdminUseCase.execute(
                RegisterAdminCommand.builder()
                        .accountId(adminAccountRegisteredEvent.accountId().toString())
                        .email(adminAccountRegisteredEvent.email())
                        .build()
        );
    }
}
