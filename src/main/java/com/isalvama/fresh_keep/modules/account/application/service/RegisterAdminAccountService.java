package com.isalvama.fresh_keep.modules.account.application.service;

import com.isalvama.fresh_keep.modules.account.application.command.RegisterAdminAccountCommand;
import com.isalvama.fresh_keep.modules.account.application.port.in.RegisterAdminAccountUseCase;
import com.isalvama.fresh_keep.modules.account.application.port.in.dto.response.AuthRegisterResult;
import com.isalvama.fresh_keep.modules.account.application.port.out.AccountRepositoryPort;
import com.isalvama.fresh_keep.modules.account.application.port.out.PasswordHasherPort;
import com.isalvama.fresh_keep.modules.account.domain.event.AdminAccountRegisteredEvent;
import com.isalvama.fresh_keep.modules.account.domain.event.AdminAccountRegisteredEventPublisher;
import com.isalvama.fresh_keep.modules.account.domain.exception.AccountAlreadyExistsException;
import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import com.isalvama.fresh_keep.shared.domain.Role;
import com.isalvama.fresh_keep.shared.domain.value_object.Email;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegisterAdminAccountService implements RegisterAdminAccountUseCase {
    private final AccountRepositoryPort accountRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;
    private final AdminAccountRegisteredEventPublisher adminAccountRegisteredEventPublisher;

    @Override
    @Transactional
    public AuthRegisterResult execute(RegisterAdminAccountCommand command) {

        Optional<Account> optionalAccount = accountRepositoryPort.findByEmail(command.email());

        if (optionalAccount.isPresent()){

            Account account = optionalAccount.get();
            if (account.hasRole(Role.ADMIN)) throw new AccountAlreadyExistsException("An admin account with the email address " + command.email() + " already exists.");

            account.addRole(Role.ADMIN);
            accountRepositoryPort.save(account);
            adminAccountRegisteredEventPublisher.publish(AdminAccountRegisteredEvent.from(account));

            return AuthRegisterResult.constitute(
                    account.getId().toString(),
                    account.getEmail().toString()
            );
        }

        String passwordHash = passwordHasherPort.hash(command.rawPassword());

        Account account = Account.createAdmin(
                Email.of(command.email()),
                passwordHash
        );

        Account savedAccount = accountRepositoryPort.save(account);

        adminAccountRegisteredEventPublisher.publish(AdminAccountRegisteredEvent.from(account));

        return AuthRegisterResult.constitute(
                savedAccount.getId().toString(),
                savedAccount.getEmail().toString()
        );
    }
}
