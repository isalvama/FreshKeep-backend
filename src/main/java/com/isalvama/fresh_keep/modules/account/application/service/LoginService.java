package com.isalvama.fresh_keep.modules.account.application.service;

import com.isalvama.fresh_keep.modules.account.application.command.LoginCommand;
import com.isalvama.fresh_keep.modules.account.application.port.in.LoginUseCase;
import com.isalvama.fresh_keep.modules.account.application.port.out.AccountRepositoryPort;
import com.isalvama.fresh_keep.modules.account.application.port.out.AuthenticationPort;
import com.isalvama.fresh_keep.modules.account.application.port.out.JwtTokenGeneratorPort;
import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.token.AuthToken;
import com.isalvama.fresh_keep.modules.account.infrastructure.web.dto.response.AuthJwtResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    private final AuthenticationPort authenticatorPort;
    private final JwtTokenGeneratorPort jwtTokenGeneratorPort;
    private final AccountRepositoryPort accountRepositoryPort;
    private final IdentityResolverService identityResolverService;

    @Transactional
    @Override
    public AuthJwtResponse execute(LoginCommand loginCommand) {
        Account account = authenticatorPort.authenticate(
                loginCommand.email(),
                loginCommand.rawPassword()
        );
        accountRepositoryPort.updateLastLogIn(account.getId(), Instant.now());

        AuthToken token = jwtTokenGeneratorPort.generateToken(account, identityResolverService.resolveFor(account));

        return AuthJwtResponse.constitute(
                account.getId().toString(),
                account.getEmail().toString(),
                token.token(),
                token.expiration()
        );
    }
}
