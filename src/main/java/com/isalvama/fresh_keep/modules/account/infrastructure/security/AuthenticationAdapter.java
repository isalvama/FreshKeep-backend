package com.isalvama.fresh_keep.modules.account.infrastructure.security;

import com.isalvama.fresh_keep.modules.account.application.port.out.AuthenticationPort;
import com.isalvama.fresh_keep.modules.account.domain.exception.DisabledAccountException;
import com.isalvama.fresh_keep.modules.account.domain.exception.InvalidCredentialsException;
import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.exception.IdentityMappingException;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.token.CustomUserPrincipal;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.token.CustomUserPrincipalMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationAdapter implements AuthenticationPort {
    private final AuthenticationManager authenticationManager;
    private final CustomUserPrincipalMapper mapper;

    @Override
    public Account authenticate(String email, String rawPassword) {
        try {
            var authResult = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            rawPassword
                    )
            );

            if (!(authResult.getPrincipal() instanceof CustomUserPrincipal principal)) {
                throw new IdentityMappingException("Authentication succeeded but principal is not of type CustomUserPrincipal");
            }

            return mapper.fromUserPrincipal(principal);

        } catch (DisabledException e) {
           throw new DisabledAccountException("Your account is disabled");
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }
}
