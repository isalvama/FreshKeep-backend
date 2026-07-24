package com.isalvama.fresh_keep.modules.account.infrastructure.security;

import com.isalvama.fresh_keep.modules.account.domain.exception.DisabledAccountException;
import com.isalvama.fresh_keep.modules.account.domain.exception.InvalidCredentialsException;
import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.exception.IdentityMappingException;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.token.CustomUserPrincipal;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.token.CustomUserPrincipalMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationAdapterTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserPrincipalMapper mapper;

    @InjectMocks
    private AuthenticationAdapter authenticationAdapter;

    private static final String EMAIL = "user@example.com";
    private static final String PASSWORD = "password123";

    @Test
    @DisplayName("Should return Account when authentication is successful")
    void authenticate_Success() {
        // Given
        CustomUserPrincipal principal = new CustomUserPrincipal("1", EMAIL, "hash", List.of("USER"));
        Authentication authentication = mock(Authentication.class);
        Account expectedAccount = mock(Account.class);

        when(authentication.getPrincipal()).thenReturn(principal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(mapper.fromUserPrincipal(principal)).thenReturn(expectedAccount);

        // When
        Account result = authenticationAdapter.authenticate(EMAIL, PASSWORD);

        // Then
        assertNotNull(result);
        assertEquals(expectedAccount, result);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Should throw DisabledAccountException when account is disabled")
    void authenticate_DisabledAccount() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new DisabledException("Account disabled"));

        // When & Then
        DisabledAccountException exception = assertThrows(DisabledAccountException.class,
                () -> authenticationAdapter.authenticate(EMAIL, PASSWORD));

        assertTrue(exception.getMessage().contains("account"));
        assertTrue(exception.getMessage().contains("disabled"));
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when credentials are wrong")
    void authenticate_InvalidCredentials() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
                () -> authenticationAdapter.authenticate(EMAIL, PASSWORD));

        assertTrue(exception.getMessage().contains("Invalid"));
        assertTrue(exception.getMessage().contains("email"));
        assertTrue(exception.getMessage().contains("password"));

    }

    @Test
    @DisplayName("Should throw IdentityMappingException when principal is of wrong type")
    void authenticate_IdentityMappingError() {
        // Given
        Authentication authentication = mock(Authentication.class);

        when(authentication.getPrincipal()).thenReturn("WrongTypePrincipal");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // When & Then
        assertThrows(IdentityMappingException.class,
                () -> authenticationAdapter.authenticate(EMAIL, PASSWORD));
    }
}