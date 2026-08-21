package com.isalvama.fresh_keep.modules.account.application.service;

import com.isalvama.fresh_keep.modules.account.application.command.RegisterAdminAccountCommand;
import com.isalvama.fresh_keep.modules.account.application.port.in.RegisterAdminAccountUseCase;
import com.isalvama.fresh_keep.modules.account.application.port.in.dto.response.AuthRegisterResult;
import com.isalvama.fresh_keep.modules.account.application.port.out.AccountRepositoryPort;
import com.isalvama.fresh_keep.modules.account.application.port.out.PasswordHasherPort;
import com.isalvama.fresh_keep.modules.account.domain.exception.AccountAlreadyExistsException;
import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import com.isalvama.fresh_keep.shared.domain.value_object.Email;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterAdminAccountService implements RegisterAdminAccountUseCase {
    private final AccountRepositoryPort accountRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;

    @Override
    @Transactional
    public AuthRegisterResult execute(RegisterAdminAccountCommand command) {

        if (accountRepositoryPort.findByEmail(command.email()).isPresent()){
            throw new AccountAlreadyExistsException("An account with the email address " + command.email() + " already exists.");
        }

        String passwordHash = passwordHasherPort.hash(command.rawPassword());

        Account account = Account.createAdmin(
                Email.of(command.email()),
                passwordHash
        );

        Account savedAccount = accountRepositoryPort.save(account);

        return AuthRegisterResult.constitute(
                savedAccount.getId().toString(),
                savedAccount.getEmail().toString()
        );
    }
}
