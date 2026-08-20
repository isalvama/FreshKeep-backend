package com.isalvama.fresh_keep.modules.account.application.service;

import com.isalvama.fresh_keep.modules.account.application.command.LoginCommand;
import com.isalvama.fresh_keep.modules.account.application.port.out.AccountRepositoryPort;
import com.isalvama.fresh_keep.modules.account.application.port.out.AuthenticationPort;
import com.isalvama.fresh_keep.modules.account.application.port.out.JwtTokenGeneratorPort;
import com.isalvama.fresh_keep.modules.account.application.port.out.dto.ResolvedEntities;
import com.isalvama.fresh_keep.modules.account.domain.exception.DisabledAccountException;
import com.isalvama.fresh_keep.modules.account.domain.exception.InvalidCredentialsException;
import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.shared.domain.value_object.Email;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.token.AuthToken;
import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import com.isalvama.fresh_keep.modules.account.infrastructure.web.dto.response.AuthJwtResponse;
import com.isalvama.fresh_keep.shared.domain.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private AuthenticationPort authenticatorPort;

    @Mock
    private JwtTokenGeneratorPort jwtTokenGeneratorPort;

    @Mock
    private AccountRepositoryPort accountRepositoryPort;

    @Mock
    private IdentityResolverService identityResolverService;

    @InjectMocks
    private LoginService loginService;

    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "Password123";
    private static final UUID ID = UUID.randomUUID();


    @Test
    @DisplayName("Should return AuthResponse when credentials are valid")
    void execute_Success() {
        // Given
        LoginCommand command = new LoginCommand(EMAIL, PASSWORD);
        Account account = createMockAccount();
        AuthToken token = new AuthToken("jwt-token", 800000L);

        when(authenticatorPort.authenticate(EMAIL, PASSWORD)).thenReturn(account);
        when(identityResolverService.resolveFor(account)).thenReturn(ResolvedEntities.constitute("userid", "adminid"));
        when(jwtTokenGeneratorPort.generateToken(account, ResolvedEntities.constitute("userid", "adminid"))).thenReturn(token);

        // When
        AuthJwtResponse response = loginService.execute(command);

        // Then
        assertNotNull(response);
        assertEquals(EMAIL, response.email());
        assertEquals("jwt-token", response.jwtString());

        verify(accountRepositoryPort).updateLastLogIn(eq(account.getId()), any(Instant.class));
        verify(authenticatorPort).authenticate(EMAIL, PASSWORD);
        verify(identityResolverService).resolveFor(account);
        verify(jwtTokenGeneratorPort).generateToken(account, ResolvedEntities.constitute("userid", "adminid"));

    }

    @Test
    @DisplayName("Should propagate exception when authentication fails")
    void execute_InvalidCredentials() {
        // Given
        LoginCommand command = new LoginCommand(EMAIL, PASSWORD);
        when(authenticatorPort.authenticate(EMAIL, PASSWORD))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        // When & Then
        assertThrows(InvalidCredentialsException.class, () -> loginService.execute(command));
        verify(authenticatorPort).authenticate(EMAIL, PASSWORD);
        verifyNoInteractions(accountRepositoryPort);
        verifyNoInteractions(identityResolverService);
        verifyNoInteractions(jwtTokenGeneratorPort);
    }

    @Test
    @DisplayName("Should propagate exception when account is inactive")
    void execute_InactiveAccount() {
        // Given
        LoginCommand command = new LoginCommand(EMAIL, PASSWORD);
        when(authenticatorPort.authenticate(EMAIL, PASSWORD))
                .thenThrow(new DisabledAccountException("Account is disabled"));

        // When & Then
        assertThrows(DisabledAccountException.class, () -> loginService.execute(command));
        verify(authenticatorPort).authenticate(EMAIL, PASSWORD);
        verifyNoInteractions(accountRepositoryPort);
        verifyNoInteractions(jwtTokenGeneratorPort);
    }

    private Account createMockAccount() {
        return Account.reconstitute(
                AccountId.of(ID),
                Email.of(EMAIL),
                "hashed password",
                Set.of(Role.USER)
        );
    }
}