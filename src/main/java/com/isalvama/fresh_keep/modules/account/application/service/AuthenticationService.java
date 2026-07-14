package com.isalvama.fresh_keep.modules.account.application.service;

import com.isalvama.fresh_keep.modules.account.application.command.RegisterAccountCommand;
import com.isalvama.fresh_keep.modules.account.application.port.in.AuthenticationUseCase;
import com.isalvama.fresh_keep.modules.account.application.port.in.dto.response.AuthResponseDto;
import com.isalvama.fresh_keep.modules.account.application.port.out.AccountRepositoryPort;
import com.isalvama.fresh_keep.modules.account.application.port.out.JwtTokenGeneratorPort;
import com.isalvama.fresh_keep.modules.account.application.port.out.PasswordHasherPort;
import com.isalvama.fresh_keep.modules.account.application.port.out.TokenRepositoryPort;
import com.isalvama.fresh_keep.modules.account.domain.exception.AccountAlreadyExistsException;
import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import com.isalvama.fresh_keep.modules.account.domain.model.Token;
import com.isalvama.fresh_keep.modules.account.domain.value_object.Email;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.AuthToken;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthenticationService implements AuthenticationUseCase {
    private final AccountRepositoryPort accountRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;
    private final JwtTokenGeneratorPort jwtTokenGeneratorPort;
    private final TokenRepositoryPort tokenRepositoryPort;

    @Override
    public AuthResponseDto register(RegisterAccountCommand command) {

        if (accountRepositoryPort.findByEmail(command.email()).isPresent()){
            throw new AccountAlreadyExistsException("an account with the email " + command.email() + " already exists.");
        }

        String passwordHash = passwordHasherPort.hash(command.rawPassword());

        Account account = Account.createUser(
                Email.of(command.email()),
                passwordHash
        );

        Account savedAccount = accountRepositoryPort.save(account);

        AuthToken jwtToken = jwtTokenGeneratorPort.generateToken(savedAccount);


        tokenRepositoryPort.save(Token.create(jwtToken, savedAccount.getId()));

        return new AuthResponseDto(savedAccount.getId().toString(), savedAccount.getEmail().toString(), jwtToken.token());
    }
}
