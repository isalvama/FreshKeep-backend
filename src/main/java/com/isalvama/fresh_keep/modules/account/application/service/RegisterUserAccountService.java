package com.isalvama.fresh_keep.modules.account.application.service;

import com.isalvama.fresh_keep.modules.account.application.command.RegisterUserAccountCommand;
import com.isalvama.fresh_keep.modules.account.application.port.in.RegisterUserAccountUseCase;
import com.isalvama.fresh_keep.modules.account.application.port.in.dto.response.AuthRegisterResult;
import com.isalvama.fresh_keep.modules.account.application.port.out.*;
import com.isalvama.fresh_keep.modules.account.domain.event.UserAccountRegisteredEvent;
import com.isalvama.fresh_keep.modules.account.domain.event.UserAccountRegisteredEventPublisher;
import com.isalvama.fresh_keep.modules.account.domain.exception.AccountAlreadyExistsException;
import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import com.isalvama.fresh_keep.shared.domain.value_object.Email;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterUserAccountService implements RegisterUserAccountUseCase {
    private final AccountRepositoryPort accountRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;
    private final UserAccountRegisteredEventPublisher accountEventPublisher;

    @Override
    @Transactional
    public AuthRegisterResult execute(RegisterUserAccountCommand command) {

        if (accountRepositoryPort.findByEmail(command.email()).isPresent()){
            throw new AccountAlreadyExistsException("An account with the email address " + command.email() + " already exists.");
        }

        String passwordHash = passwordHasherPort.hash(command.rawPassword());

        Account account = Account.createUser(
                Email.of(command.email()),
                passwordHash
        );

        Account savedAccount = accountRepositoryPort.save(account);

        accountEventPublisher.publish(UserAccountRegisteredEvent.from(savedAccount));

        return AuthRegisterResult.constitute(
                savedAccount.getId().toString(),
                savedAccount.getEmail().toString()
        );
    }
}
