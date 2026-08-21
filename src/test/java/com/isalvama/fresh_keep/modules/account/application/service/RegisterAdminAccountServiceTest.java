package com.isalvama.fresh_keep.modules.account.application.service;

import com.isalvama.fresh_keep.modules.account.application.command.RegisterAdminAccountCommand;
import com.isalvama.fresh_keep.modules.account.application.port.out.AccountRepositoryPort;
import com.isalvama.fresh_keep.modules.account.application.port.out.JwtTokenGeneratorPort;
import com.isalvama.fresh_keep.modules.account.application.port.out.PasswordHasherPort;
import com.isalvama.fresh_keep.modules.account.domain.exception.AccountAlreadyExistsException;
import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import com.isalvama.fresh_keep.modules.account.application.port.in.dto.response.AuthRegisterResult;
import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.shared.domain.Role;
import com.isalvama.fresh_keep.shared.domain.value_object.Email;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterAdminAccountServiceTest {

    private static final String email = "admin@gmail.com";
    private static final String password = "adminpassword";
    private static final RegisterAdminAccountCommand command = new RegisterAdminAccountCommand(email, password);

    @Mock
    private AccountRepositoryPort accountRepositoryPort;

    @Mock
    private PasswordHasherPort passwordHasherPort;

    @Mock
    private JwtTokenGeneratorPort jwtTokenGeneratorPort;

    @Mock
    private IdentityResolverService identityResolverService;

    @InjectMocks
    private RegisterAdminAccountService registerAdminAccountService;

    @Test
    void shouldReturnAuthRegisterResponseSuccessfully(){
        when(accountRepositoryPort.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordHasherPort.hash(password)).thenReturn(password + " hashed");
        when(accountRepositoryPort.save(any(Account.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());

        AuthRegisterResult response = registerAdminAccountService.execute(command);

        assertNotNull(response);
        assertNotNull(response.accountId());
        assertEquals(email, response.email());

        verify(accountRepositoryPort, times(1)).findByEmail(email);
        verify(passwordHasherPort, times(1)).hash(password);
        verify(accountRepositoryPort, times(1)).save(any(Account.class));
        verifyNoInteractions(jwtTokenGeneratorPort);
        verifyNoInteractions(identityResolverService);
    }

    @Test
    void shouldNotExecuteIfAccountWithMatchingEmailAlreadyExists(){
        when(accountRepositoryPort.findByEmail(email))
                .thenReturn(Optional.of(Account.reconstitute(AccountId.create(), Email.of(email), password, Set.of(Role.ADMIN))));

        Exception exception = assertThrows(AccountAlreadyExistsException.class, () -> {registerAdminAccountService.execute(command);});

        assertTrue(exception.getMessage().contains(email));
        assertTrue(exception.getMessage().contains("already exists"));
        assertTrue(exception.getMessage().contains("account with the email address"));

        verify(accountRepositoryPort, times(1)).findByEmail(email);
        verify(passwordHasherPort, never()).hash(password);
        verify(accountRepositoryPort, never()).save(any(Account.class));
    }

    @Test
    void shouldNotContinueExecutingIfPasswordHashingProcessThrowsException(){
        when(accountRepositoryPort.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordHasherPort.hash(password)).thenThrow(new RuntimeException("Error in the hashing process"));

        assertThrows(RuntimeException.class, () -> {registerAdminAccountService.execute(command);});

        verify(accountRepositoryPort, times(1)).findByEmail(email);
        verify(passwordHasherPort, times(1)).hash(password);
        verify(accountRepositoryPort, never()).save(any(Account.class));
    }

    @Test
    void shouldNotContinueExecutingIfPersistenceSavingProcessThrowsException(){
        when(accountRepositoryPort.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordHasherPort.hash(password)).thenReturn(password + " hashed");
        when(accountRepositoryPort.save(any(Account.class))).thenThrow(new RuntimeException("Error in the persistence process"));

        assertThrows(RuntimeException.class, () -> {registerAdminAccountService.execute(command);});

        verify(accountRepositoryPort, times(1)).findByEmail(email);
        verify(passwordHasherPort, times(1)).hash(password);
        verify(accountRepositoryPort, times(1)).save(any(Account.class));
    }
}
