package com.isalvama.fresh_keep.modules.account.application.service;

import com.isalvama.fresh_keep.modules.account.application.command.RegisterUserAccountCommand;
import com.isalvama.fresh_keep.modules.account.application.port.in.RegisterUserAccountUseCase;
import com.isalvama.fresh_keep.modules.account.application.port.in.dto.response.AuthResponseDto;
import com.isalvama.fresh_keep.modules.account.application.port.out.AccountRepositoryPort;
import com.isalvama.fresh_keep.modules.account.application.port.out.JwtTokenGeneratorPort;
import com.isalvama.fresh_keep.modules.account.application.port.out.PasswordHasherPort;
import com.isalvama.fresh_keep.modules.account.domain.exception.AccountAlreadyExistsException;
import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import com.isalvama.fresh_keep.modules.account.domain.value_object.Email;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.token.AuthToken;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterUserAccountService implements RegisterUserAccountUseCase {
    private final AccountRepositoryPort accountRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;
    private final JwtTokenGeneratorPort jwtTokenGeneratorPort;

    @Override
    @Transactional
    public AuthResponseDto execute(RegisterUserAccountCommand command) {

        if (accountRepositoryPort.findByEmail(command.email()).isPresent()){
            throw new AccountAlreadyExistsException("An account with the email address " + command.email() + " already exists.");
        }

        String passwordHash = passwordHasherPort.hash(command.rawPassword());

        Account account = Account.createUser(
                Email.of(command.email()),
                passwordHash
        );

        Account savedAccount = accountRepositoryPort.save(account);

        AuthToken jwtToken = jwtTokenGeneratorPort.generateToken(savedAccount);

        return new AuthResponseDto(savedAccount.getId().toString(), savedAccount.getEmail().toString(), jwtToken.token());
    }
}
