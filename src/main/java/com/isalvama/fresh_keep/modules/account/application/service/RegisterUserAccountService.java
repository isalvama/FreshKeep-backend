package com.isalvama.fresh_keep.modules.account.application.service;

import com.isalvama.fresh_keep.modules.account.application.command.RegisterUserAccountCommand;
import com.isalvama.fresh_keep.modules.account.application.port.in.RegisterUserAccountUseCase;
import com.isalvama.fresh_keep.modules.account.application.port.in.dto.response.AuthRegisterResult;
import com.isalvama.fresh_keep.modules.account.application.port.out.*;
import com.isalvama.fresh_keep.modules.account.domain.event.UserAccountRegisteredEvent;
import com.isalvama.fresh_keep.modules.account.domain.event.UserAccountRegisteredEventPublisher;
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
public class RegisterUserAccountService implements RegisterUserAccountUseCase {
    private final AccountRepositoryPort accountRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;
    private final UserAccountRegisteredEventPublisher accountEventPublisher;

    @Override
    @Transactional
    public AuthRegisterResult execute(RegisterUserAccountCommand command) {

       Optional<Account> optionalAccount = accountRepositoryPort.findByEmail(command.email());

       if (optionalAccount.isPresent()){

           Account account = optionalAccount.get();
           if (account.hasRole(Role.USER)) throw new AccountAlreadyExistsException("A user account with the email address " + command.email() + " already exists.");

           account.addRole(Role.USER);
           accountRepositoryPort.save(account);
           accountEventPublisher.publish(UserAccountRegisteredEvent.from(account));

           return AuthRegisterResult.constitute(
                   account.getId().toString(),
                   account.getEmail().toString()
           );

       }

        String passwordHash = passwordHasherPort.hash(command.rawPassword());

        Account account = Account.createUser(
                Email.of(command.email()),
                passwordHash
        );
        Account savedAccount = accountRepositoryPort.save(account);

        accountEventPublisher.publish(UserAccountRegisteredEvent.from(account));

        return AuthRegisterResult.constitute(
                savedAccount.getId().toString(),
                savedAccount.getEmail().toString()
        );
    }
}
