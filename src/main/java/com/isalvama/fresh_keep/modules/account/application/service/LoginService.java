package com.isalvama.fresh_keep.modules.account.application.service;

import com.isalvama.fresh_keep.modules.account.application.command.LoginCommand;
import com.isalvama.fresh_keep.modules.account.application.port.in.LoginUseCase;
import com.isalvama.fresh_keep.modules.account.application.port.out.AuthenticationPort;
import com.isalvama.fresh_keep.modules.account.application.port.out.JwtTokenGeneratorPort;
import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.token.AuthToken;
import com.isalvama.fresh_keep.modules.account.infrastructure.web.dto.response.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    private final AuthenticationPort authenticatorPort;
    private final JwtTokenGeneratorPort jwtTokenGeneratorPort;

    @Override
    public AuthResponse execute(LoginCommand loginCommand) {
        Account account = authenticatorPort.authenticate(
                loginCommand.email(),
                loginCommand.rawPassword()
        );
        AuthToken token = jwtTokenGeneratorPort.generateToken(account);

        return AuthResponse.constitute(
                account.getId().toString(),
                account.getEmail().toString(),
                token.token(),
                token.expiration()
        );
    }
}
